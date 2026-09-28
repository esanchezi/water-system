import { AfterViewInit, Component, OnInit, ViewChild, inject } from '@angular/core';
import { GoogleMap, MapInfoWindow, MapMarker } from '@angular/google-maps';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import Swal from 'sweetalert2';

import { PozoService } from 'src/app/modules/shared/services/pozo.service';
import { TramoService } from 'src/app/modules/shared/services/tramo.service';
import { CajaValvulaService } from 'src/app/modules/shared/services/caja-valvula.service';
import { RegistroSuministroService } from 'src/app/modules/shared/services/registro-suministro.service';
import { ValvulaFotoService } from 'src/app/modules/shared/services/valvula-foto.service';
import {
  PozoModel,
  TramoModel,
  TramoHorarioModel,
  TramoValvulaModel,
  CajaValvulaModel,
  DiasPorPozoModel,
  DiasPorTramoModel,
  ResumenValvulasModel,
  SegmentoTuberiaModel,
  MATERIALES_SEGMENTO,
  ValvulaModel
} from 'src/app/modules/shared/models/WaterValves.model';

type ModoTrazo = 'DIRECTIONS' | 'MANUAL';

@Component({
  selector: 'app-water-valves',
  templateUrl: './water-valves.component.html',
  styleUrls: ['./water-valves.component.css']
})
export class WaterValvesComponent implements OnInit, AfterViewInit {
  @ViewChild(GoogleMap) map!: GoogleMap;
  @ViewChild(MapInfoWindow) infoWindowCaja!: MapInfoWindow;

  private readonly pozoService = inject(PozoService);
  private readonly tramoService = inject(TramoService);
  private readonly cajaValvulaService = inject(CajaValvulaService);
  private readonly registroSuministroService = inject(RegistroSuministroService);
  private readonly valvulaFotoService = inject(ValvulaFotoService);
  private readonly snackBar = inject(MatSnackBar);

  // Centrado a medio camino entre los 2 pozos fijos (Los López / Buenavista).
  center: google.maps.LatLngLiteral = { lat: 21.0454893, lng: -101.5753507 };
  zoom = 14;

  // OJO: este objeto se manda tal cual al [options] del <google-map>. NO
  // se debe generar un literal `{...}` directo en el template -- eso crea
  // una referencia nueva en cada ciclo de detección de cambios, y Angular
  // vuelve a llamar setOptions() cada vez. Al llamar fitBounds() (como
  // hace enfocarPuntos()) el mapa dispara eventos ('idle'/'bounds_changed')
  // que a su vez disparan otro ciclo de detección de cambios -> nuevo
  // objeto -> setOptions() otra vez -> nuevo evento -> ... y la página se
  // "cicla" (se queda pegada/parpadeando). Por eso el objeto vive aquí,
  // con una sola referencia estable.
  mapOptions: google.maps.MapOptions = {
    disableDefaultUI: true,
    zoomControl: true,
    streetViewControl: true,
    mapTypeControl: true,
    mapTypeControlOptions: {
      style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
      position: google.maps.ControlPosition.TOP_RIGHT
    }
  };

  // Límite del área del mapa (para no perderse fuera de la zona de
  // trabajo) -- se calcula a partir de todos los puntos conocidos (pozos,
  // cajas y trazos de tramos) con un margen amplio, y se puede quitar
  // temporalmente por si necesitas ubicar algo fuera del área actual.
  restriccionMapaActiva = true;

  toggleRestriccionMapa(): void {
    this.restriccionMapaActiva = !this.restriccionMapaActiva;
    this.actualizarRestriccionMapa();
  }

  private actualizarRestriccionMapa(): void {
    if (!this.restriccionMapaActiva) {
      // Google Maps exige mandar null explícito para quitar una
      // restricción ya puesta -- simplemente no incluir la propiedad no
      // la borra.
      this.mapOptions = { ...this.mapOptions, restriction: null as unknown as google.maps.MapRestriction };
      return;
    }
    // OJO: se usa this.cajas directo (no el getter cajasConUbicacion) --
    // ese getter oculta cajas del otro lado cuando hay un tramo activo, y
    // el límite del mapa debe cubrir SIEMPRE toda el área de trabajo, sin
    // importar qué tramo esté viéndose en ese momento.
    const puntos: google.maps.LatLngLiteral[] = [
      ...this.pozosConUbicacion.map(p => ({ lat: p.lat!, lng: p.lng! })),
      ...this.cajas.filter(c => c.lat != null && c.lng != null).map(c => ({ lat: c.lat!, lng: c.lng! }))
    ];
    for (const tramo of this.tramos) {
      if (!tramo.trazoJson) {
        continue;
      }
      try {
        puntos.push(...(JSON.parse(tramo.trazoJson) as google.maps.LatLngLiteral[]));
      } catch {
        // trazo inválido, se ignora para el cálculo del límite
      }
    }
    if (puntos.length === 0) {
      return;
    }
    let minLat = Infinity, maxLat = -Infinity, minLng = Infinity, maxLng = -Infinity;
    for (const p of puntos) {
      minLat = Math.min(minLat, p.lat);
      maxLat = Math.max(maxLat, p.lat);
      minLng = Math.min(minLng, p.lng);
      maxLng = Math.max(maxLng, p.lng);
    }
    // ~1.5 km de margen para no bloquear zonas donde a futuro se agreguen
    // más cajas/pozos/tramos -- solo evita irse muy lejos del área de trabajo.
    const margen = 0.015;
    this.mapOptions = {
      ...this.mapOptions,
      restriction: {
        latLngBounds: {
          north: maxLat + margen,
          south: minLat - margen,
          east: maxLng + margen,
          west: minLng - margen
        },
        strictBounds: true
      }
    };
  }

  // Ícono propio para diferenciar los pozos en el mapa -- tamaño chico
  // (26x26, como un pin normal de Maps) mientras consigues uno que te
  // guste más. Si encuentras un ícono bonito en fonts.google.com/icons
  // (busca "water_pump" o "faucet"), Flaticon, Icons8 o SVG Repo, súbeme
  // el archivo y lo pongo directo aquí en lugar de este dibujo.
  readonly pozoIcon: google.maps.Icon = {
    url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="26" height="26" viewBox="0 0 26 26">' +
      '<circle cx="13" cy="13" r="12" fill="#6a1b9a" stroke="#4a148c" stroke-width="1"/>' +
      '<rect x="7" y="6" width="12" height="7" rx="2" fill="#ffffff"/>' +
      '<polygon points="9,13 7,20 9.5,20 11,13" fill="#ffffff"/>' +
      '<polygon points="13,13 12,20 14,20 13,13" fill="#ffffff"/>' +
      '<polygon points="17,13 15,13 17,20 19.5,20" fill="#ffffff"/>' +
      '</svg>'
    ),
    scaledSize: new google.maps.Size(26, 26),
    anchor: new google.maps.Point(13, 13)
  };

  // Ícono para las cajas de válvulas -- cuadro naranja, para distinguirlas
  // de los pozos (morado) y de las líneas de los tramos.
  readonly cajaIcon: google.maps.Icon = {
    url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 20 20">' +
      '<rect x="1" y="1" width="18" height="18" rx="3" fill="#e65100" stroke="#bf360c" stroke-width="1.5"/>' +
      '<circle cx="10" cy="10" r="4.5" fill="#ffffff"/>' +
      '</svg>'
    ),
    scaledSize: new google.maps.Size(20, 20),
    anchor: new google.maps.Point(10, 10)
  };

  // ============================================================
  // Trazo de tramo -- comparación Directions (sigue la calle real)
  // vs dibujo manual punto por punto.
  // ============================================================
  modoTrazo: ModoTrazo = 'DIRECTIONS';
  capturandoTrazo = false;
  puntosClic: google.maps.LatLngLiteral[] = [];
  trazoActual: google.maps.LatLngLiteral[] = [];
  nombreTramo = '';
  descripcionTramo = '';
  ladoTramo: string | null = null;
  guardandoTramo = false;

  // Horarios con los que se guarda el tramo nuevo -- un mismo tramo puede
  // tener más de uno (ej. Jazmín -> jueves matutino Y domingo vespertino,
  // ambos del pozo Buenavista), por eso es una lista que se va armando
  // antes de guardar el tramo.
  horariosNuevoTramo: TramoHorarioModel[] = [];
  pozoIdNuevoHorario: number | null = null;
  diaSemanaNuevoHorario: string | null = null;
  horaInicioNuevoHorario: string | null = null;
  horaFinNuevoHorario: string | null = null;

  readonly diasSemana = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'];

  // Editar el trazo de un tramo ya guardado -- se carga su trazo actual en
  // el mapa y se le pueden agregar más puntos al final (se anexan tal
  // cual, sin recalcular Directions).
  editandoTrazoExistente = false;
  tramoEditandoTrazoId: number | null = null;
  guardandoTrazoEditado = false;

  get tramoEditandoTrazoNombre(): string {
    return this.tramos.find(t => t.tramoId === this.tramoEditandoTrazoId)?.nombre ?? '';
  }

  private directionsService = new google.maps.DirectionsService();
  private directionsRenderer = new google.maps.DirectionsRenderer({ suppressMarkers: false });

  // Catálogo de tramos ya guardados, para comparar/reusar.
  tramos: TramoModel[] = [];
  tramoPreview: google.maps.LatLngLiteral[] = [];
  tramoPreviewNombre = '';

  // Pozos -- son fijos (solo 2, no cambian), se siembran una vez en el
  // backend al arrancar (PozoSeeder). Aquí solo se muestran, no se dan de
  // alta desde la pantalla.
  pozos: PozoModel[] = [];

  get pozosConUbicacion(): PozoModel[] {
    return this.pozos.filter(p => p.lat != null && p.lng != null);
  }

  guardandoUbicacionPozoId: number | null = null;

  // Cajas de válvulas
  cajas: CajaValvulaModel[] = [];
  nuevaCaja: CajaValvulaModel = this.cajaVacia();
  nuevaValvulaIdentificador = '';
  guardandoUbicacionCajaId: number | null = null;

  // Si hay un tramo activo en el mapa (visto con "ver en mapa" o elegido en
  // "Modificar un tramo"), solo se muestran las cajas que en verdad tienen
  // alguna válvula ligada a ESE tramo (tramo.valvulas) -- no todas las
  // cajas capturadas. Si el tramo no tiene válvulas asignadas todavía, se
  // muestran todas (no hay nada más específico con qué filtrar).
  get cajasConUbicacion(): CajaValvulaModel[] {
    const conUbicacion = this.cajas.filter(c => c.lat != null && c.lng != null);
    const valvulasTramo = this.tramoActivoEnMapa?.valvulas;
    if (!valvulasTramo?.length) {
      return conUbicacion;
    }
    const cajaIds = new Set(
      valvulasTramo.map(v => v.cajaId).filter((id): id is number => id != null)
    );
    return conUbicacion.filter(c => c.cajaId != null && cajaIds.has(c.cajaId));
  }

  // Resumen
  diasPorPozo: DiasPorPozoModel[] = [];
  diasPorTramo: DiasPorTramoModel[] = [];
  resumenValvulas: ResumenValvulasModel | null = null;

  ngOnInit(): void {
    this.cargarTramos();
    this.cargarPozos();
    this.cargarCajas();
    this.cargarResumen();
  }

  ngAfterViewInit(): void {
    if (this.map?.googleMap) {
      this.directionsRenderer.setMap(this.map.googleMap);
    } else {
      console.error('Map is not initialized.');
    }
  }

  // ============================================================
  // Ubicación exacta -- para levantamiento tipo catastro con tablet/celular
  // (GPS del dispositivo), usado tanto por pozos como por cajas de válvulas.
  // ============================================================

  private obtenerUbicacionActual(): Promise<google.maps.LatLngLiteral> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject('Este navegador no tiene geolocalización');
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (pos) => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
        (err) => reject(err.message || 'No se pudo obtener tu ubicación'),
        { enableHighAccuracy: true, timeout: 15000 }
      );
    });
  }

  // Centra el mapa en el punto intermedio entre todos los pozos con
  // ubicación conocida -- se recalcula solo, así que si reubicas un pozo
  // el centro sigue siendo correcto.
  centrarEntrePozos(): void {
    const conUbicacion = this.pozosConUbicacion;
    if (conUbicacion.length === 0) {
      return;
    }
    const lat = conUbicacion.reduce((sum, p) => sum + (p.lat ?? 0), 0) / conUbicacion.length;
    const lng = conUbicacion.reduce((sum, p) => sum + (p.lng ?? 0), 0) / conUbicacion.length;
    this.center = { lat, lng };
    this.zoom = 14;
  }

  // ============================================================
  // Trazo
  // ============================================================

  // Colocar en el mapa la ubicación de la caja NUEVA que se está dando de
  // alta (en vez de escribir lat/lng a mano o tener que ir a pararte ahí
  // con GPS). Solo aplica a la caja que todavía no se guarda; las que ya
  // existen se reubican con GPS (para que quede la ubicación real, exacta).
  colocandoNuevaCaja = false;

  toggleColocarNuevaCaja(): void {
    this.colocandoNuevaCaja = !this.colocandoNuevaCaja;
    if (this.colocandoNuevaCaja) {
      this.capturandoTrazo = false;
    }
  }

  onMapClick(event: google.maps.MapMouseEvent): void {
    if (!event.latLng) {
      return;
    }
    if (this.colocandoNuevaCaja) {
      this.nuevaCaja.lat = event.latLng.lat();
      this.nuevaCaja.lng = event.latLng.lng();
      this.colocandoNuevaCaja = false;
      this.openSnackBar('Ubicación marcada -- revisa y guarda la caja', 'Listo');
      return;
    }
    if (!this.capturandoTrazo) {
      return;
    }
    const punto: google.maps.LatLngLiteral = { lat: event.latLng.lat(), lng: event.latLng.lng() };
    this.puntosClic.push(punto);

    // Al editar el trazo de un tramo existente, cada punto nuevo se
    // agrega tal cual al final -- no se recalcula la ruta con Directions
    // (el trazo guardado ya puede tener muchos puntos, no solo los que se
    // marcaron originalmente).
    if (this.editandoTrazoExistente || this.modoTrazo === 'MANUAL') {
      this.trazoActual = [...this.puntosClic];
    } else {
      this.calcularRutaDirections();
    }
  }

  toggleCaptura(): void {
    this.capturandoTrazo = !this.capturandoTrazo;
  }

  cambiarModo(modo: ModoTrazo): void {
    this.modoTrazo = modo;
    this.limpiarTrazo();
  }

  limpiarTrazo(): void {
    this.puntosClic = [];
    this.trazoActual = [];
    this.directionsRenderer.setMap(null);
    this.directionsRenderer = new google.maps.DirectionsRenderer({ suppressMarkers: false });
    if (this.map?.googleMap) {
      this.directionsRenderer.setMap(this.map.googleMap);
    }
  }

  quitarUltimoPunto(): void {
    if (this.puntosClic.length === 0) {
      return;
    }
    this.eliminarPunto(this.puntosClic.length - 1);
  }

  // Quita un punto específico del trazo que se está armando o editando --
  // no solo el último. Así, si un clic quedó mal puesto a la mitad del
  // trazo, se puede corregir sin tener que borrar todos los que van
  // después. Se puede llamar desde la lista de puntos o haciendo clic
  // directo en su marcador en el mapa.
  eliminarPunto(index: number): void {
    const puntosRestantes = [...this.puntosClic];
    puntosRestantes.splice(index, 1);
    this.limpiarTrazo();
    this.puntosClic = puntosRestantes;

    if (this.editandoTrazoExistente || this.modoTrazo === 'MANUAL') {
      this.trazoActual = [...this.puntosClic];
    } else if (this.puntosClic.length >= 2) {
      this.calcularRutaDirections();
    }
  }

  // ============================================================
  // Editar el trazo de un tramo ya guardado -- carga sus puntos actuales
  // y deja seguir marcando en el mapa para agregar más al final.
  // ============================================================

  iniciarEdicionTrazo(tramoId: number | null): void {
    this.tramoEditandoTrazoId = tramoId;
    if (tramoId == null) {
      this.cancelarEdicionTrazo();
      return;
    }
    const tramo = this.tramos.find(t => t.tramoId === tramoId);
    if (!tramo?.trazoJson) {
      this.openSnackBar('Este tramo no tiene trazo guardado', 'Atención');
      return;
    }
    try {
      const puntos = JSON.parse(tramo.trazoJson) as google.maps.LatLngLiteral[];
      this.editandoTrazoExistente = true;
      this.puntosClic = [...puntos];
      this.trazoActual = [...puntos];
      this.capturandoTrazo = true;
      if (puntos.length) {
        this.center = puntos[Math.floor(puntos.length / 2)];
      }
    } catch (e) {
      console.error('Trazo inválido', e);
      this.openSnackBar('No se pudo leer el trazo de este tramo', 'Error');
    }
  }

  cancelarEdicionTrazo(): void {
    this.editandoTrazoExistente = false;
    this.tramoEditandoTrazoId = null;
    this.capturandoTrazo = false;
    this.limpiarTrazo();
  }

  guardarTrazoEditado(): void {
    if (!this.tramoEditandoTrazoId) {
      return;
    }
    const tramo = this.tramos.find(t => t.tramoId === this.tramoEditandoTrazoId);
    if (!tramo) {
      return;
    }
    if (this.trazoActual.length < 2) {
      this.openSnackBar('El trazo necesita al menos 2 puntos', 'Atención');
      return;
    }
    this.guardandoTrazoEditado = true;
    const actualizado: TramoModel = { ...tramo, trazoJson: JSON.stringify(this.trazoActual) };
    this.tramoService.updateTramo(tramo.tramoId, actualizado).subscribe({
      next: (resp: any) => {
        this.guardandoTrazoEditado = false;
        if (resp?.metadata?.code === '00') {
          this.openSnackBar('Trazo actualizado', 'Éxito');
          this.cancelarEdicionTrazo();
          this.cargarTramos();
        } else {
          this.openSnackBar('No se pudo actualizar el trazo', 'Error');
        }
      },
      error: (e) => {
        this.guardandoTrazoEditado = false;
        console.error(e);
        this.openSnackBar('No se pudo actualizar el trazo', 'Error');
      }
    });
  }

  private calcularRutaDirections(): void {
    if (this.puntosClic.length < 2) {
      return;
    }
    const origin = this.puntosClic[0];
    const destination = this.puntosClic[this.puntosClic.length - 1];
    const waypoints = this.puntosClic.slice(1, -1).map(p => ({ location: p, stopover: true }));

    this.directionsService.route({
      origin,
      destination,
      waypoints,
      travelMode: google.maps.TravelMode.DRIVING,
      optimizeWaypoints: false
    }, (result, status) => {
      if (status === google.maps.DirectionsStatus.OK && result) {
        this.directionsRenderer.setDirections(result);
        const path = result.routes[0].overview_path;
        this.trazoActual = path.map(p => ({ lat: p.lat(), lng: p.lng() }));
      } else {
        console.error('Error fetching directions:', status);
        this.openSnackBar('No se pudo calcular la ruta entre esos puntos', 'Error');
      }
    });
  }

  // Nombre del pozo para mostrar en los chips de horarios (mientras no se
  // ha guardado el tramo, no hay pozoNombre que venga del backend).
  nombrePozo(pozoId: number | null): string {
    return this.pozos.find(p => p.pozoId === pozoId)?.nombre ?? '';
  }

  agregarHorarioNuevoTramo(): void {
    if (this.pozoIdNuevoHorario == null || !this.diaSemanaNuevoHorario
        || !this.horaInicioNuevoHorario || !this.horaFinNuevoHorario) {
      this.openSnackBar('Elige pozo, día y hora de inicio/fin para agregar el horario', 'Atención');
      return;
    }
    this.horariosNuevoTramo.push({
      pozoId: this.pozoIdNuevoHorario,
      pozoNombre: this.nombrePozo(this.pozoIdNuevoHorario),
      diaSemana: this.diaSemanaNuevoHorario,
      horaInicio: this.normalizarHora(this.horaInicioNuevoHorario),
      horaFin: this.normalizarHora(this.horaFinNuevoHorario)
    });
    this.pozoIdNuevoHorario = null;
    this.diaSemanaNuevoHorario = null;
    this.horaInicioNuevoHorario = null;
    this.horaFinNuevoHorario = null;
  }

  // El backend guarda hora inicio/fin como LocalTime, que exige el formato
  // exacto "HH:mm" (2 dígitos de hora) -- si llega "8:00" en vez de
  // "08:00" (puede pasar según el navegador/teclado), el guardado falla
  // sin mensaje claro. Esto lo deja siempre bien formado antes de
  // guardarlo, y de paso descarta cualquier basura extra (ej. "8:000").
  private normalizarHora(valor: string | null): string | null {
    if (!valor) {
      return null;
    }
    const match = valor.trim().match(/^(\d{1,2}):(\d{2})/);
    if (!match) {
      return valor.trim();
    }
    return `${match[1].padStart(2, '0')}:${match[2]}`;
  }

  quitarHorarioNuevoTramo(index: number): void {
    this.horariosNuevoTramo.splice(index, 1);
  }

  guardarTramo(): void {
    if (!this.nombreTramo.trim()) {
      this.openSnackBar('Ponle un nombre al tramo antes de guardar', 'Atención');
      return;
    }
    if (this.trazoActual.length < 2) {
      this.openSnackBar('Marca al menos 2 puntos en el mapa', 'Atención');
      return;
    }

    this.guardandoTramo = true;
    this.tramoService.addTramo({
      nombre: this.nombreTramo.trim(),
      descripcion: this.descripcionTramo.trim(),
      lado: this.ladoTramo,
      trazoJson: JSON.stringify(this.trazoActual),
      metodoTrazo: this.modoTrazo,
      horarios: this.horariosNuevoTramo
    }).subscribe({
      next: (resp: any) => {
        this.guardandoTramo = false;
        if (resp?.metadata?.code === '00') {
          this.openSnackBar('Tramo guardado', 'Éxito');
          this.nombreTramo = '';
          this.descripcionTramo = '';
          this.ladoTramo = null;
          this.horariosNuevoTramo = [];
          this.limpiarTrazo();
          this.cargarTramos();
        } else {
          this.openSnackBar('No se pudo guardar el tramo', 'Error');
        }
      },
      error: (e) => {
        this.guardandoTramo = false;
        console.error(e);
        this.openSnackBar('No se pudo guardar el tramo', 'Error');
      }
    });
  }

  // ============================================================
  // Modificar horario -- se elige UN tramo de una lista desplegable y se
  // le administra su lista de horarios (agregar/quitar), en vez de editar
  // todos los tramos a la vez.
  // ============================================================
  tramoSeleccionadoId: number | null = null;
  horariosEdit: TramoHorarioModel[] = [];
  pozoIdHorarioEdit: number | null = null;
  diaSemanaHorarioEdit: string | null = null;
  horaInicioHorarioEdit: string | null = null;
  horaFinHorarioEdit: string | null = null;
  guardandoHorario = false;

  // Copia local de nombre/descripción/lado del tramo elegido -- se edita
  // aparte y solo se manda al backend al guardar, junto con horarios y
  // válvulas (mismo botón, un solo PUT).
  nombreEdit = '';
  descripcionEdit = '';
  ladoEdit: string | null = null;

  onSeleccionarTramo(tramoId: number | null): void {
    this.tramoSeleccionadoId = tramoId;
    const tramo = this.tramos.find(t => t.tramoId === tramoId);
    this.nombreEdit = tramo?.nombre ?? '';
    this.descripcionEdit = tramo?.descripcion ?? '';
    this.ladoEdit = tramo?.lado ?? null;
    // Copia local -- se edita aparte y solo se manda al backend al guardar.
    this.horariosEdit = tramo?.horarios ? tramo.horarios.map(h => ({ ...h })) : [];
    this.pozoIdHorarioEdit = null;
    this.diaSemanaHorarioEdit = null;
    this.horaInicioHorarioEdit = null;
    this.horaFinHorarioEdit = null;

    this.valvulasEdit = tramo?.valvulas ? tramo.valvulas.map(v => ({ ...v })) : [];
    this.valvulaIdInstruccionEdit = null;
    this.estadoInstruccionEdit = null;

    this.segmentosEdit = tramo?.segmentos ? tramo.segmentos.map(s => ({ ...s })) : [];
    this.materialSegmentoEdit = null;
    this.diametroSegmentoEdit = '';
    this.longitudSegmentoEdit = null;

    // Al elegir un tramo del selector, acerca el mapa a su trazo real para
    // que se vea completo y centrado (en vez de dejar el zoom/centro previo),
    // y lo marca como el tramo activo para el tooltip de las cajas.
    this.tramoActivoEnMapa = tramo;
    if (tramo?.trazoJson) {
      try {
        const puntos = JSON.parse(tramo.trazoJson) as google.maps.LatLngLiteral[];
        this.enfocarPuntos(puntos);
      } catch (e) {
        console.error('Trazo inválido', e);
      }
    }
  }

  // ============================================================
  // Válvulas del tramo -- qué válvulas hay que abrir/cerrar para que este
  // tramo reciba agua. Una misma válvula puede tener distinto estado
  // requerido en tramos distintos, por eso se maneja aquí y no como un
  // atributo fijo de la válvula.
  // ============================================================
  valvulasEdit: TramoValvulaModel[] = [];
  valvulaIdInstruccionEdit: number | null = null;
  estadoInstruccionEdit: string | null = null;

  readonly estadosValvula = ['ABIERTA', 'CERRADA'];

  // Lista plana de todas las válvulas de todas las cajas, con un texto que
  // deja claro de qué caja es cada una (para elegir en el select).
  //
  // OJO: esto era un getter que armaba un arreglo (y objetos) nuevos cada
  // vez que se leía -- y el template lo lee en cada ciclo de detección de
  // cambios dentro de un *ngFor. Como cada lectura daba una referencia
  // distinta, Angular destruía y volvía a crear todas las <mat-option>
  // en cada ciclo, lo cual disparaba otro ciclo, y así indefinidamente:
  // esto era la causa real de que la página "se ciclara" justo al abrir
  // el panel de modificar (que es donde se usa este select). Por eso
  // ahora es un campo normal que se recalcula una sola vez, cuando
  // cambian las cajas (ver recalcularValvulasDisponibles()).
  valvulasDisponibles: { valvulaId: number; label: string }[] = [];

  private recalcularValvulasDisponibles(): void {
    const resultado: { valvulaId: number; label: string }[] = [];
    for (const caja of this.cajas) {
      for (const v of caja.listValvula ?? []) {
        if (v.valvulaId != null) {
          resultado.push({ valvulaId: v.valvulaId, label: `${v.identificador} (caja: ${caja.nombre})` });
        }
      }
    }
    this.valvulasDisponibles = resultado;
  }

  agregarInstruccionValvula(): void {
    if (this.valvulaIdInstruccionEdit == null || !this.estadoInstruccionEdit) {
      this.openSnackBar('Elige la válvula y el estado requerido', 'Atención');
      return;
    }
    const disponible = this.valvulasDisponibles.find(v => v.valvulaId === this.valvulaIdInstruccionEdit);
    this.valvulasEdit.push({
      valvulaId: this.valvulaIdInstruccionEdit,
      valvulaIdentificador: disponible?.label ?? '',
      estadoRequerido: this.estadoInstruccionEdit
    });
    this.valvulaIdInstruccionEdit = null;
    this.estadoInstruccionEdit = null;
  }

  quitarInstruccionValvula(index: number): void {
    this.valvulasEdit.splice(index, 1);
  }

  // ============================================================
  // Segmentos de tubería/manguera del tramo -- un mismo tramo puede
  // combinar más de un material/diámetro a lo largo de su trazo (ej. PVC
  // al inicio y manguera después), por eso es una lista ordenada, no
  // campos únicos.
  // ============================================================
  segmentosEdit: SegmentoTuberiaModel[] = [];
  materialSegmentoEdit: string | null = null;
  diametroSegmentoEdit = '';
  longitudSegmentoEdit: number | null = null;
  observacionesSegmentoEdit = '';

  readonly materialesSegmento = MATERIALES_SEGMENTO;

  agregarSegmentoEdit(): void {
    if (!this.materialSegmentoEdit) {
      this.openSnackBar('Elige el material del segmento', 'Atención');
      return;
    }
    this.segmentosEdit.push({
      orden: this.segmentosEdit.length,
      material: this.materialSegmentoEdit,
      diametro: this.diametroSegmentoEdit.trim() || null,
      longitudMetros: this.longitudSegmentoEdit,
      observaciones: this.observacionesSegmentoEdit.trim() || null
    });
    this.materialSegmentoEdit = null;
    this.diametroSegmentoEdit = '';
    this.longitudSegmentoEdit = null;
    this.observacionesSegmentoEdit = '';
  }

  quitarSegmentoEdit(index: number): void {
    this.segmentosEdit.splice(index, 1);
    // Reacomoda el orden para que siga siendo 0,1,2... sin huecos.
    this.segmentosEdit.forEach((s, i) => s.orden = i);
  }

  moverSegmentoEdit(index: number, direccion: -1 | 1): void {
    const destino = index + direccion;
    if (destino < 0 || destino >= this.segmentosEdit.length) {
      return;
    }
    [this.segmentosEdit[index], this.segmentosEdit[destino]] = [this.segmentosEdit[destino], this.segmentosEdit[index]];
    this.segmentosEdit.forEach((s, i) => s.orden = i);
  }

  get tramoSeleccionado(): TramoModel | undefined {
    return this.tramos.find(t => t.tramoId === this.tramoSeleccionadoId);
  }

  agregarHorarioEdit(): void {
    if (this.pozoIdHorarioEdit == null || !this.diaSemanaHorarioEdit
        || !this.horaInicioHorarioEdit || !this.horaFinHorarioEdit) {
      this.openSnackBar('Elige pozo, día y hora de inicio/fin para agregar el horario', 'Atención');
      return;
    }
    this.horariosEdit.push({
      pozoId: this.pozoIdHorarioEdit,
      pozoNombre: this.nombrePozo(this.pozoIdHorarioEdit),
      diaSemana: this.diaSemanaHorarioEdit,
      horaInicio: this.normalizarHora(this.horaInicioHorarioEdit),
      horaFin: this.normalizarHora(this.horaFinHorarioEdit)
    });
    this.pozoIdHorarioEdit = null;
    this.diaSemanaHorarioEdit = null;
    this.horaInicioHorarioEdit = null;
    this.horaFinHorarioEdit = null;
  }

  quitarHorarioEdit(index: number): void {
    this.horariosEdit.splice(index, 1);
  }

  // Manda el tramo completo (trazo incluido) con nombre/descripción/lado,
  // horarios y válvulas actualizados -- todo en un solo PUT, para no
  // perder el trazo ya dibujado.
  guardarHorariosSeleccionado(): void {
    const tramo = this.tramoSeleccionado;
    if (!tramo?.tramoId) {
      return;
    }
    if (!this.nombreEdit.trim()) {
      this.openSnackBar('El tramo necesita un nombre', 'Atención');
      return;
    }
    this.guardandoHorario = true;
    const actualizado: TramoModel = {
      ...tramo,
      nombre: this.nombreEdit.trim(),
      descripcion: this.descripcionEdit.trim(),
      lado: this.ladoEdit,
      horarios: this.horariosEdit,
      valvulas: this.valvulasEdit,
      segmentos: this.segmentosEdit
    };
    this.tramoService.updateTramo(tramo.tramoId, actualizado).subscribe({
      next: (resp: any) => {
        this.guardandoHorario = false;
        if (resp?.metadata?.code === '00') {
          this.openSnackBar('Tramo guardado', 'Éxito');
          this.cargarTramos();
          this.cargarResumen();
        } else {
          this.openSnackBar(resp?.metadata?.message || 'No se pudieron guardar los cambios', 'Error');
        }
      },
      error: (e) => {
        this.guardandoHorario = false;
        console.error(e);
        const mensaje = e?.error?.metadata?.message || e?.error?.message;
        this.openSnackBar(mensaje || 'No se pudieron guardar los horarios y válvulas', 'Error');
      }
    });
  }

  // Tramo completo que se está viendo en el mapa ahora (por "ver en mapa"
  // o por el selector del panel de modificar) -- se usa para saber, en el
  // tooltip de una caja de válvulas, si cada válvula debe estar abierta o
  // cerrada para ESE tramo.
  tramoActivoEnMapa: TramoModel | undefined;

  // Select junto al mapa para ver un tramo sin tener que bajar hasta la
  // tabla de "Tramos guardados" y darle al icono de ojo -- no hace falta
  // recortar el mapa para esto, solo cabe en la fila de botones de abajo.
  tramoSeleccionadoParaVerId: number | null = null;

  onSeleccionarTramoParaVer(tramoId: number | null): void {
    const tramo = this.tramos.find(t => t.tramoId === tramoId);
    if (tramo) {
      this.verTramoEnMapa(tramo);
    }
  }

  verTramoEnMapa(tramo: TramoModel): void {
    if (!tramo.trazoJson) {
      return;
    }
    try {
      const puntos = JSON.parse(tramo.trazoJson) as google.maps.LatLngLiteral[];
      this.tramoPreview = puntos;
      this.tramoPreviewNombre = tramo.nombre;
      this.tramoActivoEnMapa = tramo;
      this.enfocarPuntos(puntos);
    } catch (e) {
      console.error('Trazo inválido', e);
    }
  }

  // Centra y hace zoom para que se vea completo un trazo. IMPORTANTE: esto
  // NO usa map.fitBounds() -- llamar a ese método imperativo dispara
  // eventos nativos del mapa ('idle'/'bounds_changed') que retriggerean la
  // detección de cambios de Angular, la cual vuelve a evaluar los bindings
  // del <google-map> y puede terminar en un ciclo (la página "se cicla").
  // En su lugar se calcula el centro y el zoom a mano y se asignan a los
  // campos `center`/`zoom` de este componente, que son los que ya están
  // enlazados con [center]/[zoom] -- así todo pasa por el binding normal
  // de Angular, sin tocar el mapa nativo directamente.
  private enfocarPuntos(puntos: google.maps.LatLngLiteral[]): void {
    if (!puntos.length) {
      return;
    }
    if (puntos.length === 1) {
      this.center = puntos[0];
      this.zoom = 20;
      return;
    }
    let minLat = Infinity, maxLat = -Infinity, minLng = Infinity, maxLng = -Infinity;
    for (const p of puntos) {
      minLat = Math.min(minLat, p.lat);
      maxLat = Math.max(maxLat, p.lat);
      minLng = Math.min(minLng, p.lng);
      maxLng = Math.max(maxLng, p.lng);
    }
    const centerLat = (minLat + maxLat) / 2;
    const centerLng = (minLng + maxLng) / 2;
    this.center = { lat: centerLat, lng: centerLng };

    // Distancia aproximada (en km) del lado más largo del recuadro que
    // encierra el trazo, para elegir un zoom que lo muestre bien cerca.
    const kmPorGradoLat = 111.32;
    const kmPorGradoLng = 111.32 * Math.cos((centerLat * Math.PI) / 180);
    const altoKm = (maxLat - minLat) * kmPorGradoLat;
    const anchoKm = (maxLng - minLng) * kmPorGradoLng;
    const distanciaKm = Math.max(altoKm, anchoKm);
    this.zoom = this.zoomSegunDistancia(distanciaKm);
  }

  // Puntos de zoom propios (más cercanos que el "auto-zoom" de Maps),
  // para que un tramo corto se vea bien acercado y uno largo no quede tan
  // lejos como para perder de vista las calles.
  private zoomSegunDistancia(km: number): number {
    if (km < 0.08) return 20;
    if (km < 0.15) return 19;
    if (km < 0.3) return 18;
    if (km < 0.6) return 17.5;
    if (km < 1) return 17;
    if (km < 2) return 16;
    return 15;
  }

  quitarPreview(): void {
    this.tramoPreview = [];
    this.tramoPreviewNombre = '';
    this.tramoActivoEnMapa = undefined;
  }

  eliminarTramo(tramo: TramoModel): void {
    Swal.fire({
      title: `¿Eliminar el tramo "${tramo.nombre}"?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) {
        return;
      }
      this.tramoService.deleteTramo(tramo.tramoId).subscribe({
        next: () => {
          this.openSnackBar('Tramo eliminado', 'Éxito');
          this.cargarTramos();
        },
        error: (e) => {
          console.error(e);
          this.openSnackBar('No se pudo eliminar el tramo', 'Error');
        }
      });
    });
  }

  // Hay tramos casi idénticos entre sí (mismo trazo, con solo un tramo más
  // agregado en otra calle) -- en vez de volver a dibujar todo desde cero,
  // esto copia trazo, horarios y válvulas de un tramo existente, y solo
  // pide el nombre del nuevo.
  duplicarTramo(tramo: TramoModel): void {
    Swal.fire({
      title: 'Duplicar tramo',
      text: `Se va a crear una copia de "${tramo.nombre}" con el mismo trazo, horarios y válvulas -- después puedes ajustarla.`,
      icon: 'question',
      input: 'text',
      inputLabel: 'Nombre del nuevo tramo',
      inputValue: `${tramo.nombre} (copia)`,
      inputValidator: (value) => (!value || !value.trim()) ? 'Ponle un nombre al tramo' : undefined,
      showCancelButton: true,
      confirmButtonText: 'Duplicar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) {
        return;
      }
      this.tramoService.addTramo({
        nombre: (result.value as string).trim(),
        descripcion: tramo.descripcion,
        lado: tramo.lado,
        trazoJson: tramo.trazoJson,
        metodoTrazo: tramo.metodoTrazo,
        horarios: tramo.horarios ?? [],
        valvulas: tramo.valvulas ?? [],
        segmentos: tramo.segmentos ?? []
      }).subscribe({
        next: (resp: any) => {
          if (resp?.metadata?.code === '00') {
            this.openSnackBar('Tramo duplicado', 'Éxito');
            this.cargarTramos();
            this.cargarResumen();
          } else {
            this.openSnackBar(resp?.metadata?.message || 'No se pudo duplicar el tramo', 'Error');
          }
        },
        error: (e) => {
          console.error(e);
          this.openSnackBar('No se pudo duplicar el tramo', 'Error');
        }
      });
    });
  }

  private cargarTramos(): void {
    this.tramoService.getListTramo().subscribe({
      next: (resp: any) => {
        this.tramos = resp?.data ?? [];
        this.recalcularValvulasPorCajaPorTramo();
        this.actualizarRestriccionMapa();
        this.recalcularTramosFiltrados();
      },
      error: (e) => console.error(e)
    });
  }

  // ============================================================
  // Filtro por día/pozo -- se usa tanto en la tabla "Tramos guardados"
  // como en el select del mapa, para no tener que ir bajando la lista a
  // mano. "Sin asignar" ayuda a encontrar tramos a los que todavía no se
  // les puso ningún horario (para irlos completando).
  // ============================================================
  filtroDiaTramos: string | 'SIN_ASIGNAR' | null = null;
  filtroPozoIdTramos: number | 'SIN_ASIGNAR' | null = null;

  // Calculado una sola vez cuando cambian los tramos o los filtros -- NO
  // como getter leído en un *ngFor (mismo motivo de siempre: un getter así
  // arma un arreglo nuevo en cada ciclo de detección de cambios y puede
  // volver a ciclar la página).
  tramosFiltrados: TramoModel[] = [];

  private tramoPasaFiltro(tramo: TramoModel): boolean {
    if (this.filtroDiaTramos === 'SIN_ASIGNAR' || this.filtroPozoIdTramos === 'SIN_ASIGNAR') {
      return !tramo.horarios?.length;
    }
    if (!this.filtroDiaTramos && this.filtroPozoIdTramos == null) {
      return true;
    }
    return !!tramo.horarios?.some(h =>
      (!this.filtroDiaTramos || h.diaSemana === this.filtroDiaTramos) &&
      (this.filtroPozoIdTramos == null || h.pozoId === this.filtroPozoIdTramos)
    );
  }

  recalcularTramosFiltrados(): void {
    this.tramosFiltrados = this.tramos.filter(t => this.tramoPasaFiltro(t));
  }

  quitarFiltrosTramos(): void {
    this.filtroDiaTramos = null;
    this.filtroPozoIdTramos = null;
    this.recalcularTramosFiltrados();
  }

  // Para la tabla de "Tramos guardados": agrupa las válvulas de cada tramo
  // por caja (tramo -> caja -> válvula, 3 niveles) para que se lea claro
  // cuando un tramo tiene varias válvulas repartidas en distintas cajas.
  // Se calcula una sola vez aquí (no como getter leído en el *ngFor de la
  // tabla) para no repetir el problema de antes: un getter que arma
  // arreglos/objetos nuevos en cada ciclo de detección de cambios hace que
  // Angular destruya y recree las filas sin parar.
  valvulasPorCajaPorTramo: { [tramoId: number]: { cajaId?: number; cajaNombre: string; lado: string | null; valvulas: TramoValvulaModel[] }[] } = {};

  // Lados posibles para una caja de válvulas -- solo aplica a calles con 2
  // redes independientes (ej. la principal); si no se especifica, la caja
  // simplemente no entra al filtro de lado.
  readonly ladosCaja = ['IZQUIERDO', 'DERECHO'];

  private recalcularValvulasPorCajaPorTramo(): void {
    const resultado: { [tramoId: number]: { cajaId?: number; cajaNombre: string; lado: string | null; valvulas: TramoValvulaModel[] }[] } = {};
    for (const tramo of this.tramos) {
      if (!tramo.tramoId) {
        continue;
      }
      const grupos: { cajaId?: number; cajaNombre: string; lado: string | null; valvulas: TramoValvulaModel[] }[] = [];
      for (const v of tramo.valvulas ?? []) {
        const cajaNombre = v.cajaNombre || 'Sin caja';
        let grupo = grupos.find(g => g.cajaId === v.cajaId && g.cajaNombre === cajaNombre);
        if (!grupo) {
          grupo = { cajaId: v.cajaId, cajaNombre, lado: v.cajaLado ?? null, valvulas: [] };
          grupos.push(grupo);
        }
        grupo.valvulas.push(v);
      }
      this.ordenarCajasDesdeElPozo(tramo, grupos);
      resultado[tramo.tramoId] = grupos;
    }
    this.valvulasPorCajaPorTramo = resultado;
    this.recalcularFiltrosLado();
  }

  // Filtro de "lado" (izquierdo/derecho) por tramo -- para calles con 2
  // redes, así no se ven todas las cajas mezcladas en una sola lista. Se
  // guarda como campos normales (no getters) para no recalcular arreglos
  // nuevos en cada ciclo de detección de cambios dentro del *ngFor.
  ladosDisponiblesPorTramo: { [tramoId: number]: string[] } = {};
  gruposVisiblesPorTramo: { [tramoId: number]: { cajaId?: number; cajaNombre: string; lado: string | null; valvulas: TramoValvulaModel[] }[] } = {};
  private ladoFiltroPorTramo: { [tramoId: number]: string | null } = {};

  private recalcularFiltrosLado(): void {
    const ladosPorTramo: { [tramoId: number]: string[] } = {};
    const gruposVisibles: { [tramoId: number]: { cajaId?: number; cajaNombre: string; lado: string | null; valvulas: TramoValvulaModel[] }[] } = {};
    for (const tramoIdStr of Object.keys(this.valvulasPorCajaPorTramo)) {
      const tramoId = Number(tramoIdStr);
      const grupos = this.valvulasPorCajaPorTramo[tramoId];
      ladosPorTramo[tramoId] = Array.from(new Set(grupos.map(g => g.lado).filter((l): l is string => !!l)));
      const filtroActual = this.ladoFiltroPorTramo[tramoId] ?? null;
      gruposVisibles[tramoId] = filtroActual ? grupos.filter(g => g.lado === filtroActual) : grupos;
    }
    this.ladosDisponiblesPorTramo = ladosPorTramo;
    this.gruposVisiblesPorTramo = gruposVisibles;
  }

  setLadoFiltro(tramoId: number, lado: string | null): void {
    this.ladoFiltroPorTramo[tramoId] = lado;
    this.recalcularFiltrosLado();
  }

  ladoFiltroActivo(tramoId: number): string | null {
    return this.ladoFiltroPorTramo[tramoId] ?? null;
  }

  // El primer punto del trazo de un tramo es justo donde está el pozo (ahí
  // empieza el tramo), así que ordena las cajas de ese tramo por qué tan
  // cerca están de ese punto -- así la lista sigue el mismo recorrido que
  // hace el agua, de donde entra hacia adelante.
  private ordenarCajasDesdeElPozo(
    tramo: TramoModel,
    grupos: { cajaId?: number; cajaNombre: string; lado: string | null; valvulas: TramoValvulaModel[] }[]
  ): void {
    if (!tramo.trazoJson || grupos.length < 2) {
      return;
    }
    let inicio: google.maps.LatLngLiteral | null = null;
    try {
      const puntos = JSON.parse(tramo.trazoJson) as google.maps.LatLngLiteral[];
      inicio = puntos[0] ?? null;
    } catch {
      inicio = null;
    }
    if (!inicio) {
      return;
    }
    const distanciaAlInicio = (cajaId: number | undefined): number => {
      if (cajaId == null) {
        return Infinity;
      }
      const caja = this.cajas.find(c => c.cajaId === cajaId);
      if (caja?.lat == null || caja?.lng == null) {
        return Infinity;
      }
      const dLat = caja.lat - inicio!.lat;
      const dLng = caja.lng - inicio!.lng;
      return (dLat * dLat) + (dLng * dLng);
    };
    grupos.sort((a, b) => distanciaAlInicio(a.cajaId) - distanciaAlInicio(b.cajaId));
  }

  // Control de qué se ve expandido en la tabla de tramos: qué tramos
  // muestran sus cajas, y qué combinación tramo+caja muestra sus válvulas.
  private cajasVisiblesPorTramo = new Set<number>();
  private valvulasVisiblesPorCaja = new Set<string>();

  toggleCajasTramo(tramoId: number): void {
    if (this.cajasVisiblesPorTramo.has(tramoId)) {
      this.cajasVisiblesPorTramo.delete(tramoId);
    } else {
      this.cajasVisiblesPorTramo.add(tramoId);
    }
  }

  cajasVisibles(tramoId: number): boolean {
    return this.cajasVisiblesPorTramo.has(tramoId);
  }

  toggleValvulasCaja(tramoId: number, cajaId: number | undefined): void {
    const key = `${tramoId}_${cajaId}`;
    if (this.valvulasVisiblesPorCaja.has(key)) {
      this.valvulasVisiblesPorCaja.delete(key);
    } else {
      this.valvulasVisiblesPorCaja.add(key);
    }
  }

  valvulasVisibles(tramoId: number, cajaId: number | undefined): boolean {
    return this.valvulasVisiblesPorCaja.has(`${tramoId}_${cajaId}`);
  }

  // ============================================================
  // Pozos
  // ============================================================

  private cargarPozos(): void {
    this.pozoService.getListPozo().subscribe({
      next: (resp: any) => {
        this.pozos = resp?.data ?? [];
        this.centrarEntrePozos();
        this.actualizarRestriccionMapa();
      },
      error: (e) => console.error(e)
    });
  }

  // Confirmación reutilizada tanto al arrastrar el pin en el mapa como al
  // reubicar con GPS -- en ambos casos hay que preguntar antes de mover,
  // mostrando de qué pozo se trata, para no reubicar el equivocado sin
  // querer (en la tablet es fácil tocar/arrastrar por accidente).
  private confirmarReubicarPozo(pozo: PozoModel): Promise<boolean> {
    return Swal.fire({
      title: `¿Reubicar el pozo "${pozo.nombre}" aquí?`,
      text: pozo.observaciones || 'Se va a mover su ubicación guardada.',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, reubicar aquí',
      cancelButtonText: 'Cancelar'
    }).then(result => result.isConfirmed);
  }

  // Arrastrar el pin del pozo en el mapa lo reubica -- pero solo tras
  // confirmar; si se cancela, se regresa a su posición anterior (si no, el
  // pin se quedaría donde lo arrastraron aunque los datos no cambiaron).
  onPozoDragEnd(pozo: PozoModel, event: google.maps.MapMouseEvent): void {
    if (!event.latLng) {
      return;
    }
    const latAnterior = pozo.lat;
    const lngAnterior = pozo.lng;
    const lat = event.latLng.lat();
    const lng = event.latLng.lng();
    this.confirmarReubicarPozo(pozo).then(confirmado => {
      if (!confirmado) {
        pozo.lat = latAnterior;
        pozo.lng = lngAnterior;
        return;
      }
      pozo.lat = lat;
      pozo.lng = lng;
      this.guardarUbicacionPozo(pozo);
    });
  }

  // Reubicar de verdad es con GPS -- vas al pozo con la tablet/celular, tocas
  // el botón, y se guarda directo con las coordenadas de donde estás
  // parada. Nada de marcar puntos en el mapa a ojo.
  usarUbicacionActualPozo(pozo: PozoModel): void {
    this.confirmarReubicarPozo(pozo).then(confirmado => {
      if (!confirmado) {
        return;
      }
      this.obtenerUbicacionActual().then(coords => {
        pozo.lat = coords.lat;
        pozo.lng = coords.lng;
        this.guardarUbicacionPozo(pozo);
      }).catch(err => {
        console.error(err);
        this.openSnackBar('No se pudo obtener tu ubicación: ' + err, 'Error');
      });
    });
  }

  guardarUbicacionPozo(pozo: PozoModel): void {
    if (!pozo.pozoId) {
      return;
    }
    this.guardandoUbicacionPozoId = pozo.pozoId;
    this.pozoService.updatePozo(pozo.pozoId, pozo).subscribe({
      next: (resp: any) => {
        this.guardandoUbicacionPozoId = null;
        if (resp?.metadata?.code === '00') {
          this.openSnackBar('Ubicación del pozo guardada', 'Éxito');
          this.cargarPozos();
        } else {
          this.openSnackBar('No se pudo guardar la ubicación', 'Error');
        }
      },
      error: (e) => {
        this.guardandoUbicacionPozoId = null;
        console.error(e);
        this.openSnackBar('No se pudo guardar la ubicación', 'Error');
      }
    });
  }

  // ============================================================
  // Cajas de válvulas
  // ============================================================

  private cajaVacia(): CajaValvulaModel {
    return { nombre: '', codigo: '', tramoId: null, listValvula: [] };
  }

  private cargarCajas(): void {
    this.cajaValvulaService.getListCajaValvula().subscribe({
      next: (resp: any) => {
        this.cajas = resp?.data ?? [];
        this.recalcularValvulasDisponibles();
        // El orden de las cajas por tramo depende de la ubicación de cada
        // caja, así que se recalcula también aquí (las cajas pueden cargar
        // después de los tramos, o cambiar de ubicación).
        this.recalcularValvulasPorCajaPorTramo();
        this.actualizarRestriccionMapa();
        this.cargarFotoUrls();
      },
      error: (e) => console.error(e)
    });
  }

  // ============================================================
  // Fotos de válvulas -- el archivo real vive en el servidor (fuera de la
  // carpeta del proyecto), no en la base de datos; aquí solo se cachean
  // las URLs locales (blob) ya descargadas, para no volver a pedir la
  // misma foto en cada ciclo de detección de cambios (mismo motivo por el
  // que valvulasDisponibles dejó de ser un getter -- ver esa nota arriba).
  fotoUrls: { [fotoId: number]: string } = {};
  subiendoFotoValvulaId: number | null = null;

  private cargarFotoUrls(): void {
    for (const caja of this.cajas) {
      for (const v of caja.listValvula ?? []) {
        for (const foto of v.fotos ?? []) {
          if (this.fotoUrls[foto.fotoId]) {
            continue;
          }
          this.valvulaFotoService.getArchivoBlob(foto.fotoId).subscribe({
            next: (blob) => {
              this.fotoUrls[foto.fotoId] = URL.createObjectURL(blob);
            },
            error: (e) => console.error('No se pudo cargar la foto', foto.fotoId, e)
          });
        }
      }
    }
  }

  subirFotoValvula(valvula: ValvulaModel, event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo || !valvula.valvulaId) {
      return;
    }
    this.subiendoFotoValvulaId = valvula.valvulaId;
    this.valvulaFotoService.subirFoto(valvula.valvulaId, archivo).subscribe({
      next: (resp: any) => {
        this.subiendoFotoValvulaId = null;
        input.value = '';
        if (resp?.metadata?.code === '00') {
          this.openSnackBar('Foto agregada', 'Éxito');
          this.cargarCajas();
        } else {
          this.openSnackBar(resp?.metadata?.message || 'No se pudo subir la foto', 'Error');
        }
      },
      error: (e) => {
        this.subiendoFotoValvulaId = null;
        input.value = '';
        console.error(e);
        this.openSnackBar('No se pudo subir la foto', 'Error');
      }
    });
  }

  eliminarFotoValvula(fotoId: number): void {
    Swal.fire({
      title: '¿Eliminar esta foto?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) {
        return;
      }
      this.valvulaFotoService.eliminarFoto(fotoId).subscribe({
        next: () => {
          if (this.fotoUrls[fotoId]) {
            URL.revokeObjectURL(this.fotoUrls[fotoId]);
            delete this.fotoUrls[fotoId];
          }
          this.openSnackBar('Foto eliminada', 'Éxito');
          this.cargarCajas();
        },
        error: (e) => {
          console.error(e);
          this.openSnackBar('No se pudo eliminar la foto', 'Error');
        }
      });
    });
  }

  agregarValvulaANuevaCaja(): void {
    if (!this.nuevaValvulaIdentificador.trim()) {
      return;
    }
    this.nuevaCaja.listValvula = this.nuevaCaja.listValvula || [];
    this.nuevaCaja.listValvula.push({ identificador: this.nuevaValvulaIdentificador.trim(), estado: 'ABIERTA' });
    this.nuevaValvulaIdentificador = '';
  }

  quitarValvulaDeNuevaCaja(index: number): void {
    this.nuevaCaja.listValvula?.splice(index, 1);
  }

  // Toma la ubicación GPS del dispositivo para la caja que se está dando
  // de alta (o para una ya guardada, si se le pasa esa en su lugar).
  usarUbicacionActualNuevaCaja(): void {
    this.obtenerUbicacionActual().then(coords => {
      this.nuevaCaja.lat = coords.lat;
      this.nuevaCaja.lng = coords.lng;
      this.openSnackBar('Ubicación capturada', 'Listo');
    }).catch(err => {
      console.error(err);
      this.openSnackBar('No se pudo obtener tu ubicación: ' + err, 'Error');
    });
  }

  // Confirmación reutilizada tanto al arrastrar el pin en el mapa como al
  // reubicar con GPS -- muestra nombre, código, lado y válvulas de la caja
  // para estar seguro de cuál es antes de moverla, y no reubicar la
  // equivocada por error en la tablet.
  private confirmarReubicarCaja(caja: CajaValvulaModel): Promise<boolean> {
    const valvulas = (caja.listValvula ?? []).map(v => v.identificador).filter(Boolean).join(', ');
    return Swal.fire({
      title: '¿Reubicar esta caja aquí?',
      html: `<strong>${caja.nombre}</strong>`
        + (caja.codigo ? `<br>Código: ${caja.codigo}` : '')
        + (caja.lado ? `<br>Lado: ${caja.lado}` : '')
        + (valvulas ? `<br>Válvulas: ${valvulas}` : '<br>(sin válvulas dadas de alta)'),
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, reubicar aquí',
      cancelButtonText: 'Cancelar'
    }).then(result => result.isConfirmed);
  }

  // Igual que con el pozo: reubicar una caja es con GPS, no marcando un
  // punto en el mapa -- se guarda directo con la ubicación capturada.
  usarUbicacionActualCaja(caja: CajaValvulaModel): void {
    this.confirmarReubicarCaja(caja).then(confirmado => {
      if (!confirmado) {
        return;
      }
      this.obtenerUbicacionActual().then(coords => {
        caja.lat = coords.lat;
        caja.lng = coords.lng;
        this.guardarUbicacionCaja(caja);
      }).catch(err => {
        console.error(err);
        this.openSnackBar('No se pudo obtener tu ubicación: ' + err, 'Error');
      });
    });
  }

  // Arrastrar el pin de la caja en el mapa la reubica -- pero solo tras
  // confirmar; si se cancela, se regresa a su posición anterior (si no, el
  // pin se quedaría donde la arrastraron aunque los datos no cambiaron).
  onCajaDragEnd(caja: CajaValvulaModel, event: google.maps.MapMouseEvent): void {
    if (!event.latLng) {
      return;
    }
    const latAnterior = caja.lat;
    const lngAnterior = caja.lng;
    const lat = event.latLng.lat();
    const lng = event.latLng.lng();
    this.confirmarReubicarCaja(caja).then(confirmado => {
      if (!confirmado) {
        caja.lat = latAnterior;
        caja.lng = lngAnterior;
        return;
      }
      caja.lat = lat;
      caja.lng = lng;
      this.guardarUbicacionCaja(caja);
    });
  }

  // Ajustar arrastrando el pin de vista previa de la caja NUEVA (todavía
  // sin guardar) -- no se guarda en el backend, solo actualiza el
  // formulario, ya que la caja como tal no existe hasta darle "Guardar".
  onNuevaCajaDragEnd(event: google.maps.MapMouseEvent): void {
    if (!event.latLng) {
      return;
    }
    this.nuevaCaja.lat = event.latLng.lat();
    this.nuevaCaja.lng = event.latLng.lng();
  }

  // Al hacer clic en el pin de una caja en el mapa, se abre un globo con
  // sus válvulas -- y si hay un tramo activo (visto con "ver en mapa" o
  // elegido en el panel de modificar), muestra si cada una debe estar
  // abierta o cerrada para ese tramo en concreto.
  cajaSeleccionadaInfo: CajaValvulaModel | null = null;

  abrirInfoCaja(caja: CajaValvulaModel, marker: MapMarker): void {
    this.cajaSeleccionadaInfo = caja;
    this.infoWindowCaja.open(marker);
  }

  estadoValvulaParaTramoActivo(valvulaId: number | undefined): string | null {
    if (valvulaId == null || !this.tramoActivoEnMapa?.valvulas) {
      return null;
    }
    const instruccion = this.tramoActivoEnMapa.valvulas.find(v => v.valvulaId === valvulaId);
    return instruccion?.estadoRequerido ?? null;
  }

  guardarUbicacionCaja(caja: CajaValvulaModel): void {
    this.guardarCambiosCaja(caja, 'Ubicación de la caja guardada');
  }

  // Guarda la caja completa (incluye su listValvula) -- se reutiliza tanto
  // para la ubicación como para agregar/quitar válvulas de una caja que
  // ya existe, ya que el backend siempre espera el objeto completo.
  private guardarCambiosCaja(caja: CajaValvulaModel, mensajeExito: string): void {
    if (!caja.cajaId) {
      return;
    }
    this.guardandoUbicacionCajaId = caja.cajaId;
    this.cajaValvulaService.updateCajaValvula(caja.cajaId, caja).subscribe({
      next: (resp: any) => {
        this.guardandoUbicacionCajaId = null;
        if (resp?.metadata?.code === '00') {
          this.openSnackBar(mensajeExito, 'Éxito');
          this.cargarCajas();
          this.cargarResumen();
          // El "lado" de la caja se refleja en las válvulas de los tramos
          // (tramo.valvulas[].cajaLado) -- hay que recargar tramos también
          // para que el filtro por lado en "Tramos guardados" quede al día.
          this.cargarTramos();
        } else {
          this.openSnackBar(resp?.metadata?.message || 'No se pudo guardar la caja', 'Error');
        }
      },
      error: (e) => {
        this.guardandoUbicacionCajaId = null;
        console.error(e);
        const mensaje = e?.error?.metadata?.message || e?.error?.message;
        this.openSnackBar(mensaje || 'No se pudo guardar la caja', 'Error');
      }
    });
  }

  // Identificador de la nueva válvula que se está escribiendo para cada
  // caja ya guardada (una por caja, por eso es un mapa por cajaId).
  nuevaValvulaPorCaja: { [cajaId: number]: string } = {};

  agregarValvulaACajaExistente(caja: CajaValvulaModel): void {
    if (!caja.cajaId) {
      return;
    }
    const identificador = (this.nuevaValvulaPorCaja[caja.cajaId] || '').trim();
    if (!identificador) {
      return;
    }
    caja.listValvula = caja.listValvula || [];
    caja.listValvula.push({ identificador, estado: 'ABIERTA' });
    this.nuevaValvulaPorCaja[caja.cajaId] = '';
    this.guardarCambiosCaja(caja, 'Válvula agregada a la caja');
  }

  quitarValvulaDeCajaExistente(caja: CajaValvulaModel, index: number): void {
    caja.listValvula?.splice(index, 1);
    this.guardarCambiosCaja(caja, 'Válvula quitada de la caja');
  }

  guardarCaja(): void {
    if (!this.nuevaCaja.nombre.trim()) {
      this.openSnackBar('Ponle un nombre a la caja de válvulas', 'Atención');
      return;
    }
    // Si no se agregó ninguna válvula a mano, se da de alta una por
    // defecto con el mismo nombre de la caja -- toda caja debe tener al
    // menos una válvula que abrir/cerrar.
    if (!this.nuevaCaja.listValvula || this.nuevaCaja.listValvula.length === 0) {
      this.nuevaCaja.listValvula = [{ identificador: this.nuevaCaja.nombre.trim(), estado: 'ABIERTA' }];
    }
    this.cajaValvulaService.addCajaValvula(this.nuevaCaja).subscribe({
      next: () => {
        this.openSnackBar('Caja de válvulas guardada', 'Éxito');
        this.nuevaCaja = this.cajaVacia();
        this.cargarCajas();
        this.cargarResumen();
      },
      error: (e) => {
        console.error(e);
        this.openSnackBar('No se pudo guardar la caja de válvulas', 'Error');
      }
    });
  }

  eliminarCaja(caja: CajaValvulaModel): void {
    Swal.fire({
      title: `¿Eliminar la caja "${caja.nombre}"?`,
      text: 'También se eliminarán sus válvulas.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) {
        return;
      }
      this.cajaValvulaService.deleteCajaValvula(caja.cajaId as number).subscribe({
        next: () => {
          this.openSnackBar('Caja eliminada', 'Éxito');
          this.cargarCajas();
          this.cargarResumen();
        },
        error: (e) => {
          console.error(e);
          this.openSnackBar('No se pudo eliminar la caja', 'Error');
        }
      });
    });
  }

  // ============================================================
  // Resumen
  // ============================================================

  private cargarResumen(): void {
    this.registroSuministroService.getDiasPorPozo().subscribe({
      next: (resp: any) => { this.diasPorPozo = resp?.data ?? []; },
      error: (e) => console.error(e)
    });
    this.registroSuministroService.getDiasPorTramo().subscribe({
      next: (resp: any) => { this.diasPorTramo = resp?.data ?? []; },
      error: (e) => console.error(e)
    });
    this.cajaValvulaService.getResumen().subscribe({
      next: (resp: any) => { this.resumenValvulas = resp?.data?.[0] ?? null; },
      error: (e) => console.error(e)
    });
  }

  openSnackBar(message: string, action: string): MatSnackBarRef<SimpleSnackBar> {
    return this.snackBar.open(message, action, { duration: 2500 });
  }
}
