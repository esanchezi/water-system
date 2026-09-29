import { Component, OnInit, inject } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import Swal from 'sweetalert2';
import { HouseService } from '../../../shared/services/house.service';
import { AvisoResponsablePagoService } from '../../../shared/services/aviso-responsable-pago.service';
import { WaterHouseModel } from '../../../shared/models/WaterUser.model';
import { AvisoResponsablePagoModel, AvisoResponsablePagoPersonaModel, CasaUsuarioCuotaModel } from '../../../shared/models/AvisoResponsablePago.model';
import { TIPOS_ENTREGA } from '../../../shared/models/AvisoAdeudo.model';
import { AvisoResponsablePagoEntregaDialogComponent } from '../../components/aviso-responsable-pago-entrega-dialog/aviso-responsable-pago-entrega-dialog.component';

// Aviso sobre personas responsables de pago del domicilio (Art. 5, 15, 15
// Bis y 15 Ter) -- a diferencia de las demás cartas, se arma por CASA, no
// por usuario: se busca la casa directo (algunas personas que viven ahí
// pueden no estar dadas de alta todavía), se llena el motivo/fecha, y se va
// armando la tabla de personas/familias responsables de pago -- libres
// (nombre a mano) o eligiendo a alguien que ya está registrado en esa
// misma casa (con su cuota vigente, para tenerla a la vista al decidir).
@Component({
  selector: 'app-aviso-responsable-pago-list',
  templateUrl: './aviso-responsable-pago-list.component.html',
  styleUrls: ['./aviso-responsable-pago-list.component.css']
})
export class AvisoResponsablePagoListComponent implements OnInit {

  private readonly houseService = inject(HouseService);
  private readonly avisoResponsablePagoService = inject(AvisoResponsablePagoService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  private readonly activatedRoute = inject(ActivatedRoute);

  // Si se llega desde la ficha de la casa (botón "Generar aviso"), viene la
  // casa preseleccionada por queryParam -- se resuelve en cuanto termina de
  // cargar el catálogo completo de casas.
  private casaIdPreseleccionada: number | null = null;

  private todasLasCasas: WaterHouseModel[] = [];

  casaSearchCtrl = new FormControl('');
  casaSearchResults: WaterHouseModel[] = [];
  casaSeleccionada: WaterHouseModel | null = null;

  usuariosCasa: CasaUsuarioCuotaModel[] = [];
  cargandoUsuariosCasa = false;

  motivoSolicitud = '';
  fechaSolicitud = new Date().toISOString().substring(0, 10);
  observacionesComite = '';

  personas: AvisoResponsablePagoPersonaModel[] = [];

  // Formulario para agregar un renglón nuevo a la tabla de personas --
  // aguaUsuarioIdNuevo != null significa que se eligió a alguien ya
  // registrado en esta casa (ver agregarUsuarioSeleccionado()); si se deja
  // vacío, se agrega como persona libre con el nombre capturado a mano.
  nuevoNombre = '';
  nuevoParentesco = '';
  nuevoFamiliaCuota: number | null = null;
  aguaUsuarioIdNuevo: number | null = null;
  cuotaVigenteNueva: number | null = null;

  generando = false;

  historial: AvisoResponsablePagoModel[] = [];
  cargandoHistorial = false;
  mostrarCanceladas = false;
  tiposEntrega = TIPOS_ENTREGA;

  get historialVisible(): AvisoResponsablePagoModel[] {
    return this.mostrarCanceladas ? this.historial : this.historial.filter(a => !a.cancelado);
  }

  // Usuarios de la casa que todavía no se han agregado a la tabla de
  // personas -- para no ofrecerlos dos veces en el selector.
  get usuariosCasaDisponibles(): CasaUsuarioCuotaModel[] {
    const idsYaAgregados = new Set(this.personas.map(p => p.aguaUsuarioId).filter((id): id is number => !!id));
    return this.usuariosCasa.filter(u => !idsYaAgregados.has(u.aguaUsuarioId));
  }

  ngOnInit(): void {
    const casaIdParam = this.activatedRoute.snapshot.queryParams?.['casaId'];
    this.casaIdPreseleccionada = casaIdParam ? Number(casaIdParam) : null;

    this.cargarCasas();
    this.casaSearchCtrl.valueChanges.subscribe(valor => this.filtrarCasas(valor));
  }

  private cargarCasas(): void {
    this.houseService.getListWaterHouse().subscribe({
      next: (resp: any) => {
        // El endpoint de casas no está envuelto en BaseRestResponse -- ver
        // WaterHouseController -- regresa el arreglo directo.
        this.todasLasCasas = Array.isArray(resp) ? resp : (resp?.data || []);

        if (this.casaIdPreseleccionada != null) {
          const casa = this.todasLasCasas.find(c => c.casaId === this.casaIdPreseleccionada);
          if (casa) this.seleccionarCasa(casa);
          this.casaIdPreseleccionada = null;
        }
      },
      error: (e: any) => console.error(e)
    });
  }

  private filtrarCasas(termino: string | null): void {
    const valor = (termino || '').toString().trim().toLowerCase();
    if (valor.length < 1) {
      this.casaSearchResults = [];
      return;
    }
    this.casaSearchResults = this.todasLasCasas.filter(c => {
      const casaNoTexto = String(c.casaNo ?? '');
      const calle = (c.calle || '').toLowerCase();
      const nombre = (c.nombre || '').toLowerCase();
      return casaNoTexto.includes(valor) || calle.includes(valor) || nombre.includes(valor);
    }).slice(0, 20);
  }

  displayCasa = (casa: WaterHouseModel): string => {
    if (!casa) return '';
    const lado = (casa as any).lado ? '-' + (casa as any).lado : '';
    return casa.casaNo ? `Casa ${casa.casaNo}${lado} -- ${casa.calle || ''}` : (casa.nombre || '');
  };

  onCasaSeleccionada(event: MatAutocompleteSelectedEvent): void {
    const casa: WaterHouseModel = event.option.value;
    this.seleccionarCasa(casa);
  }

  seleccionarCasa(casa: WaterHouseModel): void {
    this.casaSeleccionada = casa;
    this.personas = [];
    this.limpiarFormularioPersonaNueva();
    this.cargarUsuariosDeLaCasa(casa.casaId);
    this.cargarHistorialDeLaCasa(casa.casaId);
  }

  quitarCasaSeleccionada(): void {
    this.casaSeleccionada = null;
    this.usuariosCasa = [];
    this.personas = [];
    this.historial = [];
    this.casaSearchCtrl.setValue('');
  }

  private cargarUsuariosDeLaCasa(casaId: number): void {
    this.cargandoUsuariosCasa = true;
    this.avisoResponsablePagoService.getUsuariosDeLaCasa(casaId).subscribe({
      next: (resp: any) => {
        this.cargandoUsuariosCasa = false;
        if (resp.metadata?.code === '00') {
          this.usuariosCasa = resp.data || [];
        }
      },
      error: (e: any) => {
        this.cargandoUsuariosCasa = false;
        console.error(e);
      }
    });
  }

  private cargarHistorialDeLaCasa(casaId: number): void {
    this.cargandoHistorial = true;
    this.avisoResponsablePagoService.getHistorialPorCasa(casaId).subscribe({
      next: (resp: any) => {
        this.cargandoHistorial = false;
        if (resp.metadata?.code === '00') {
          this.historial = resp.data || [];
        }
      },
      error: (e: any) => {
        this.cargandoHistorial = false;
        console.error(e);
      }
    });
  }

  // Al elegir a alguien del selector de usuarios ya registrados -- autofill
  // del nombre (no editable, es el del sistema) y de la cuota vigente, solo
  // para que se vea de un vistazo antes de agregarlo.
  onUsuarioExistenteSeleccionado(aguaUsuarioId: number | null): void {
    this.aguaUsuarioIdNuevo = aguaUsuarioId;
    const usuario = this.usuariosCasa.find(u => u.aguaUsuarioId === aguaUsuarioId);
    this.nuevoNombre = usuario ? usuario.nombreCompleto : '';
    this.cuotaVigenteNueva = usuario?.cuotaVigente ?? null;
  }

  agregarPersona(): void {
    if (!this.nuevoNombre.trim()) {
      this.openSnackBar('Captura el nombre de la persona', 'Atención');
      return;
    }
    const usuario = this.aguaUsuarioIdNuevo != null
      ? this.usuariosCasa.find(u => u.aguaUsuarioId === this.aguaUsuarioIdNuevo)
      : null;

    this.personas.push({
      nombreCompleto: this.nuevoNombre.trim(),
      parentesco: this.nuevoParentesco.trim() || undefined,
      familiaCuota: this.nuevoFamiliaCuota ?? undefined,
      aguaUsuarioId: usuario?.aguaUsuarioId,
      noUsuario: usuario?.noUsuario,
      cuotaVigenteSnapshot: usuario?.cuotaVigente
    });

    this.limpiarFormularioPersonaNueva();
  }

  quitarPersona(index: number): void {
    this.personas.splice(index, 1);
  }

  private limpiarFormularioPersonaNueva(): void {
    this.nuevoNombre = '';
    this.nuevoParentesco = '';
    this.nuevoFamiliaCuota = null;
    this.aguaUsuarioIdNuevo = null;
    this.cuotaVigenteNueva = null;
  }

  generar(): void {
    if (!this.casaSeleccionada) {
      this.openSnackBar('Busca y elige la casa primero', 'Atención');
      return;
    }
    if (this.personas.length === 0) {
      this.openSnackBar('Agrega al menos una persona a la tabla', 'Atención');
      return;
    }

    Swal.fire({
      title: `¿Generar el Aviso de Responsables de Pago?`,
      html: `Casa ${this.casaSeleccionada.casaNo} -- ${this.personas.length} persona(s). Se le asigna folio consecutivo y queda registrado en el historial.`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, generar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.confirmarGeneracion();
    });
  }

  private confirmarGeneracion(): void {
    if (!this.casaSeleccionada) return;
    this.generando = true;

    this.avisoResponsablePagoService.generar({
      casaId: this.casaSeleccionada.casaId,
      motivoSolicitud: this.motivoSolicitud.trim() || undefined,
      // Se manda "naive" (sin Z ni offset) -- mismo criterio que el resto
      // de la app, para que no se corra un día.
      fechaSolicitud: this.fechaSolicitud || undefined,
      observacionesComite: this.observacionesComite.trim() || undefined,
      personas: this.personas
    }).subscribe({
      next: (resp) => {
        this.generando = false;
        const blob = resp.body;
        if (!blob) {
          this.openSnackBar('No se recibió el PDF generado', 'Error');
          return;
        }
        this.descargarBlob(blob, 'aviso_responsable_pago.pdf');
        this.openSnackBar('Aviso generado correctamente', 'Éxito');

        this.personas = [];
        this.motivoSolicitud = '';
        this.observacionesComite = '';
        this.limpiarFormularioPersonaNueva();
        if (this.casaSeleccionada) {
          this.cargarHistorialDeLaCasa(this.casaSeleccionada.casaId);
        }
      },
      error: (e: any) => {
        this.generando = false;
        console.error(e);
        const mensaje = e?.headers?.get?.('X-Error-Message') || 'No se pudo generar el aviso';
        this.openSnackBar(mensaje, 'Error');
      }
    });
  }

  etiquetaTipoEntrega(valor: string | undefined): string {
    return this.tiposEntrega.find(t => t.valor === valor)?.etiqueta ?? valor ?? '';
  }

  formatearFechaNaive(fechaISO: string | undefined): string {
    if (!fechaISO) return '';
    const [anio, mes, dia] = fechaISO.substring(0, 10).split('-');
    return `${dia}/${mes}/${anio}`;
  }

  registrarEntrega(aviso: AvisoResponsablePagoModel): void {
    const dialogRef = this.dialog.open(AvisoResponsablePagoEntregaDialogComponent, {
      width: '480px',
      data: { aviso }
    });
    dialogRef.afterClosed().subscribe((guardado: boolean) => {
      if (guardado && this.casaSeleccionada) {
        this.openSnackBar('Entrega registrada', 'Éxito');
        this.cargarHistorialDeLaCasa(this.casaSeleccionada.casaId);
      }
    });
  }

  // Reabre el mismo dialog solo para consultar/agregar/quitar las fotos de
  // respaldo de una entrega ya registrada -- ver AvisoAdeudoListComponent,
  // mismo patrón.
  verEvidenciaFotografica(aviso: AvisoResponsablePagoModel): void {
    this.dialog.open(AvisoResponsablePagoEntregaDialogComponent, {
      width: '480px',
      data: { aviso, soloVerFotos: true }
    });
  }

  cancelarAviso(aviso: AvisoResponsablePagoModel): void {
    Swal.fire({
      title: `¿Cancelar el folio ${aviso.folioNotificacion}?`,
      text: 'Se oculta del historial, pero se puede consultar con "Mostrar canceladas".',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, cancelar',
      cancelButtonText: 'No'
    }).then(result => {
      if (!result.isConfirmed || !this.casaSeleccionada) return;
      this.avisoResponsablePagoService.cancelar(aviso.responsablePagoId).subscribe({
        next: () => {
          this.openSnackBar('Aviso cancelado', 'Éxito');
          if (this.casaSeleccionada) this.cargarHistorialDeLaCasa(this.casaSeleccionada.casaId);
        },
        error: (e: any) => {
          console.error(e);
          this.openSnackBar('No se pudo cancelar el aviso', 'Error');
        }
      });
    });
  }

  reactivarAviso(aviso: AvisoResponsablePagoModel): void {
    this.avisoResponsablePagoService.reactivar(aviso.responsablePagoId).subscribe({
      next: () => {
        this.openSnackBar('Aviso reactivado', 'Éxito');
        if (this.casaSeleccionada) this.cargarHistorialDeLaCasa(this.casaSeleccionada.casaId);
      },
      error: (e: any) => {
        console.error(e);
        this.openSnackBar('No se pudo reactivar el aviso', 'Error');
      }
    });
  }

  private descargarBlob(blob: Blob, nombreArchivo: string): void {
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombreArchivo;
    enlace.click();
    URL.revokeObjectURL(url);
  }

  private openSnackBar(message: string, action: string): void {
    this.snackBar.open(message, action, { duration: 4000 });
  }
}
