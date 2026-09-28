import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { Router } from '@angular/router';
import { SelectionModel } from '@angular/cdk/collections';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin, Observable } from 'rxjs';
import Swal from 'sweetalert2';
import { AvisoAdeudoService } from '../../../shared/services/aviso-adeudo.service';
import { CatalogService } from '../../../shared/services/catalog.service';
import { CatalogOptionModel } from '../../../shared/models/Catalog.model';
import {
  AdeudoLuzUsuarioModel,
  AvisoAdeudoAtencionModel,
  AvisoAdeudoModel,
  RESULTADOS_ATENCION,
  TIPOS_AVISO,
  TIPOS_ENTREGA,
  UsuarioNoRegistradoModel
} from '../../../shared/models/AvisoAdeudo.model';
import { AvisoAdeudoEntregaDialogComponent } from '../../components/aviso-adeudo-entrega-dialog/aviso-adeudo-entrega-dialog.component';
import { AvisoAdeudoAtencionDialogComponent } from '../../../shared/components/aviso-adeudo-atencion-dialog/aviso-adeudo-atencion-dialog.component';

// Catálogo de calles -- mismo id que ya usa el módulo de Usuarios/Casas
// para la cascada Zona (Sección) -> Calle.
const CATALOGO_CALLES_ID = 15;

// Sentinela para "Todas las calles" en el select de Calle -- distinto de
// `null` (que significa "todavía no elegiste nada"). Al elegir esto se
// vuelve a calcular el adeudo de TODOS los usuarios activos (lento, pero
// necesario para encontrar usuarios sin calle de catálogo asignada en su
// casa, que de otro modo nunca aparecerían bajo ninguna calle).
const TODAS_LAS_CALLES = -1;

// Estatus del Comité (catálogo) que marca a un usuario como dado de baja.
// Por default estos NO se muestran en la lista (no tiene caso mandarles
// carta de adeudo), pero siguen viniendo del backend para poder verlos si
// se elige explícitamente ese estatus en el filtro.
const ESTATUS_DADO_DE_BAJA_ID = 102;

// Igual que dado de baja: si el estatus del Comité contiene "corte" (ej.
// "Corte", "Con corte"), tampoco se muestra por default en candidatos --
// ya se le cortó el servicio, no tiene caso seguir mandándole avisos hasta
// que se reconecte. Se compara por texto (no por id, no lo tenemos
// hardcodeado) para no depender del id exacto del catálogo.
function esEstatusCorte(nombre: string | null | undefined): boolean {
  return !!nombre && nombre.toLowerCase().includes('corte');
}

@Component({
  selector: 'app-aviso-adeudo-list',
  templateUrl: './aviso-adeudo-list.component.html',
  styleUrls: ['./aviso-adeudo-list.component.css']
})
export class AvisoAdeudoListComponent implements OnInit {

  private readonly avisoAdeudoService = inject(AvisoAdeudoService);
  private readonly catalogService = inject(CatalogService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  displayColumns: string[] = [
    'select', 'calleNombre', 'casaNo', 'noUsuario', 'nombreCompleto', 'estatusComiteNombre',
    'periodosAdeudadosTexto', 'adeudoTotal', 'noFolioUltimoPago', 'fechaUltimoPago', 'ultimoAviso'
  ];

  historialColumns: string[] = [
    'seleccionSegundo', 'folioNotificacion', 'tipoAviso', 'noUsuario', 'nombreUsuarioTitular', 'periodosAdeudados', 'adeudoTotal',
    'noFolioUltimoPago', 'fechaUltimoPago', 'dateAdd', 'entrega', 'atencion', 'acciones'
  ];

  // Checkboxes del historial para generar el Segundo aviso en lote -- solo
  // aplica a Primeros avisos "Pendiente de cobro" (entregado, sin atender,
  // sin un Segundo posterior ya generado), el mismo criterio que ya usa la
  // leyenda de esa columna y el botón individual "Generar Segundo".
  avisosSeleccionadosSegundo: Set<number> = new Set();

  resultadosAtencion = RESULTADOS_ATENCION;

  dataSource = new MatTableDataSource<AdeudoLuzUsuarioModel>();
  selection = new SelectionModel<AdeudoLuzUsuarioModel>(true, []);

  tiposAviso = TIPOS_AVISO;
  tipoAviso: string = 'PRIMERO';
  // Fecha completa (día+mes+año) en la que debe presentarse en el Comité --
  // se pide siempre antes de generar, para llenar "Debe presentarse el día
  // ___ de ___ de 20__" de la carta. Antes solo se pedía el día (1-31) y se
  // asumía el mes/año actual, lo cual salía mal si se generaba a fin de mes
  // para una cita el mes siguiente.
  fechaPresentacion: string | null = null;

  // Cascada Zona -> Calle -- calcular el adeudo es pesado (recorre varios
  // años para cada usuario), así que ya no se calcula para TODOS los
  // usuarios activos al entrar a la pantalla. Se espera a que elijan zona
  // y calle, y solo entonces se piden -- y calculan -- los candidatos de
  // esa calle (mismo patrón de catálogos que ya usa el módulo de Usuarios).
  zonas: CatalogOptionModel[] = [];
  private todasLasCalles: CatalogOptionModel[] = [];
  calles: CatalogOptionModel[] = [];
  zonaIdSeleccionada: number | null = null;
  calleIdSeleccionada: number | null = null;

  casaFiltro = '';

  // Filtro por estatus del Comité -- para identificar de un vistazo a
  // quiénes ya tienen corte (mismo patrón que ya usa Deudores).
  estatusComiteFiltro = '';
  estatusComiteOpciones: string[] = [];

  cargando = false;
  generando = false;
  // true en cuanto se elige una calle al menos una vez -- para distinguir
  // "todavía no has buscado" de "ya buscaste y no hay candidatos".
  yaSeBusco = false;

  totalAdeudoSeleccionado = 0;

  // Personas SIN usuario en el sistema (censo en proceso) -- se agregan a
  // mano (solo nombre + dirección opcional) y se generan junto con los
  // usuarios seleccionados de la tabla, con la cuota fija anual del
  // backend. No tienen calle/casa/estatus, así que se muestran aparte en
  // vez de mezclarse con las filas de la tabla de candidatos.
  noRegistrados: UsuarioNoRegistradoModel[] = [];
  nuevoNoRegistradoNombre = '';
  nuevoNoRegistradoDireccion = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  // Historial de avisos ya generados -- para no tener que adivinar quién
  // ya recibió un primer/segundo aviso.
  historial: AvisoAdeudoModel[] = [];
  cargandoHistorial = false;
  // Las canceladas se ocultan por default (mismo patrón que los usuarios
  // dados de baja en candidatos), pero se pueden consultar con este toggle.
  mostrarCanceladas = false;
  tiposEntrega = TIPOS_ENTREGA;

  get historialVisible(): AvisoAdeudoModel[] {
    return this.mostrarCanceladas ? this.historial : this.historial.filter(a => !a.cancelada);
  }

  // true si este Primer aviso ya quedó superado por un Segundo aviso
  // ACTIVO generado después, para el mismo usuario -- ya no tiene caso
  // cancelarlo ni volver a generarle el Segundo desde aquí (ver leyenda y
  // botones ocultos en el historial). Se calcula por folio consecutivo
  // (mayor = más reciente), no por fecha, porque es el mismo criterio que
  // ya usa el folio para todo lo demás en este módulo.
  // Link directo a la ficha completa del usuario -- misma convención que ya
  // usan Personas y Deudores (details-user vuelve a pedir todos los datos
  // con solo el usuarioId, no hace falta mandar nada más).
  irADetalleUsuario(aguaUsuarioId?: number): void {
    if (!aguaUsuarioId) return;
    this.router.navigate(['dashboard/detailsUser'], {
      queryParams: { element: JSON.stringify({ usuarioId: aguaUsuarioId }) }
    });
  }

  tieneSegundoPosterior(aviso: AvisoAdeudoModel): boolean {
    if (aviso.tipoAviso !== 'PRIMERO' || !aviso.aguaUsuarioId) return false;
    return this.historial.some(a =>
      a.tipoAviso === 'SEGUNDO'
      && a.aguaUsuarioId === aviso.aguaUsuarioId
      && a.folioNotificacion > aviso.folioNotificacion
      && !a.cancelada
    );
  }

  // Mismo criterio que la leyenda "Pendiente de cobro" de la columna
  // Cobro/Atención -- un Primer aviso ya entregado, sin marcar como
  // atendido, y que no haya quedado superado por un Segundo posterior.
  puedeGenerarSegundo(aviso: AvisoAdeudoModel): boolean {
    return !aviso.cancelada && aviso.tipoAviso === 'PRIMERO' && !!aviso.entregado
      && !aviso.atendido && !this.tieneSegundoPosterior(aviso);
  }

  toggleSeleccionSegundo(avisoAdeudoId: number): void {
    if (this.avisosSeleccionadosSegundo.has(avisoAdeudoId)) {
      this.avisosSeleccionadosSegundo.delete(avisoAdeudoId);
    } else {
      this.avisosSeleccionadosSegundo.add(avisoAdeudoId);
    }
  }

  etiquetaTipoEntrega(valor: string | undefined): string {
    return this.tiposEntrega.find(t => t.valor === valor)?.etiqueta ?? valor ?? '';
  }

  etiquetaResultadoAtencion(valor: string | undefined): string {
    return this.resultadosAtencion.find(r => r.valor === valor)?.etiqueta ?? valor ?? '';
  }

  // "dd/MM/yyyy" armado con puro texto (substring de la parte de fecha del
  // ISO string), en vez del date pipe de Angular -- fechaEntrega es una
  // fecha "naive" (sin zona horaria, ver aviso-adeudo-entrega-dialog), y el
  // date pipe la interpreta según la zona del navegador, lo que podía
  // mostrar un día antes del que realmente se eligió.
  formatearFechaNaive(fechaISO: string | undefined): string {
    if (!fechaISO) return '';
    const [anio, mes, dia] = fechaISO.substring(0, 10).split('-');
    return `${dia}/${mes}/${anio}`;
  }

  ngOnInit(): void {
    this.loadZonasYCalles();
  }

  private loadZonasYCalles(): void {
    this.catalogService.getOptionsByClave('SECCIONES_COLONIA').subscribe({
      next: (opts) => this.zonas = [...opts].sort((a, b) => a.nombre.localeCompare(b.nombre)),
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptions(CATALOGO_CALLES_ID).subscribe({
      next: (opts) => {
        this.todasLasCalles = [...opts].sort((a, b) => a.nombre.localeCompare(b.nombre));
        this.calles = this.todasLasCalles;
      },
      error: (e: any) => console.error(e)
    });
  }

  // Al elegir zona se filtra el dropdown de Calle a solo las de esa zona
  // (mismo patrón que en Usuarios), y se limpian la calle y los resultados
  // que se hubieran cargado antes.
  onZonaChange(zonaId: number | null): void {
    this.zonaIdSeleccionada = zonaId;
    this.calleIdSeleccionada = null;
    this.calles = zonaId != null
      ? this.todasLasCalles.filter(c => c.zonaId === zonaId)
      : this.todasLasCalles;
    this.limpiarResultados();
  }

  onCalleChange(calleId: number | null): void {
    this.calleIdSeleccionada = calleId;
    if (calleId === TODAS_LAS_CALLES) {
      this.cargarCandidatos(this.avisoAdeudoService.getCandidatos());
    } else if (calleId != null) {
      this.cargarCandidatos(this.avisoAdeudoService.getCandidatosPorCalle(calleId));
    } else {
      this.limpiarResultados();
    }
  }

  private limpiarResultados(): void {
    this.yaSeBusco = false;
    this.dataSource = new MatTableDataSource<AdeudoLuzUsuarioModel>();
    this.estatusComiteFiltro = '';
    this.estatusComiteOpciones = [];
    this.selection.clear();
    this.actualizarTotalSeleccionado();
  }

  // Compartido entre "una calle" y "todas las calles" -- solo cambia el
  // observable de dónde vienen los datos.
  private cargarCandidatos(fuente: Observable<any>): void {
    this.cargando = true;
    this.yaSeBusco = true;
    this.selection.clear();
    fuente.subscribe({
      next: (resp: any) => {
        this.cargando = false;
        if (resp.metadata?.code === '00') {
          const data: AdeudoLuzUsuarioModel[] = resp.data || [];
          this.dataSource = new MatTableDataSource<AdeudoLuzUsuarioModel>(data);
          this.dataSource.paginator = this.paginator;
          this.dataSource.filterPredicate = (row: AdeudoLuzUsuarioModel, filter: string) => {
            const f = JSON.parse(filter);
            const matchCasa = !f.casa || String(row.casaNo ?? '').includes(f.casa);
            // Si no se eligió un estatus específico, se ocultan los dados
            // de baja y los que ya tienen corte por default -- solo
            // aparecen si se filtra justo por ese estatus.
            const matchEstatus = f.estatus
              ? row.estatusComiteNombre === f.estatus
              : row.estatusComiteId !== ESTATUS_DADO_DE_BAJA_ID && !esEstatusCorte(row.estatusComiteNombre);
            return matchCasa && matchEstatus;
          };

          this.estatusComiteOpciones = [...new Set(
            data.map(d => d.estatusComiteNombre).filter((v): v is string => !!v)
          )].sort();

          this.applyFilters();
        } else {
          this.openSnackBar(resp?.metadata?.message || 'No se pudieron cargar los candidatos', 'Error');
        }
        this.actualizarTotalSeleccionado();
      },
      error: (e: any) => {
        this.cargando = false;
        console.error(e);
        this.openSnackBar('No se pudieron cargar los candidatos', 'Error');
      }
    });
  }

  private recargarCandidatosActuales(): void {
    if (this.calleIdSeleccionada === TODAS_LAS_CALLES) {
      this.cargarCandidatos(this.avisoAdeudoService.getCandidatos());
    } else if (this.calleIdSeleccionada != null) {
      this.cargarCandidatos(this.avisoAdeudoService.getCandidatosPorCalle(this.calleIdSeleccionada));
    }
  }

  private cargarHistorial(): void {
    this.cargandoHistorial = true;
    this.avisosSeleccionadosSegundo.clear();
    this.avisoAdeudoService.getHistorial().subscribe({
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

  toggleHistorial(abierto: boolean): void {
    if (abierto && this.historial.length === 0) {
      this.cargarHistorial();
    }
  }

  registrarEntrega(aviso: AvisoAdeudoModel): void {
    const dialogRef = this.dialog.open(AvisoAdeudoEntregaDialogComponent, {
      width: '480px',
      data: { aviso }
    });
    dialogRef.afterClosed().subscribe((guardado: boolean) => {
      if (guardado) {
        this.openSnackBar('Entrega registrada', 'Éxito');
        this.cargarHistorial();
      }
    });
  }

  // Reabre el mismo dialog de entrega, pero en modo solo-consulta (no se
  // vuelve a capturar la entrega, ver AvisoAdeudoEntregaDialogComponent) --
  // es la única forma de ver las fotos de respaldo después de haberlas
  // subido, ya que el botón "Marcar como entregada" desaparece en cuanto
  // entregado = true.
  verEvidenciaFotografica(aviso: AvisoAdeudoModel): void {
    this.dialog.open(AvisoAdeudoEntregaDialogComponent, {
      width: '480px',
      data: { aviso, soloVerFotos: true }
    });
  }

  // Abre el dialog para capturar cómo se resolvió (pagado/condonado/
  // convenio/otro) + comentario opcional + folio del recibo opcional, y
  // marca el aviso como atendido con esos datos. Mismo dialog que usa la
  // alerta de la ficha de usuario (ver AvisoAdeudoAtencionDialogComponent).
  marcarAtendida(aviso: AvisoAdeudoModel): void {
    const dialogRef = this.dialog.open(AvisoAdeudoAtencionDialogComponent, {
      width: '420px',
      data: { avisos: [aviso] }
    });
    dialogRef.afterClosed().subscribe((datos: AvisoAdeudoAtencionModel | null) => {
      if (!datos) return;
      this.avisoAdeudoService.marcarAtendida(aviso.avisoAdeudoId, datos).subscribe({
        next: () => {
          this.openSnackBar('Marcado como atendido', 'Éxito');
          this.cargarHistorial();
        },
        error: (e: any) => {
          console.error(e);
          const mensaje = e?.error?.metadata?.message || 'No se pudo marcar como atendido';
          this.openSnackBar(mensaje, 'Error');
        }
      });
    });
  }

  cancelarAviso(aviso: AvisoAdeudoModel): void {
    Swal.fire({
      title: `¿Cancelar el folio ${aviso.folioNotificacion}?`,
      text: `${aviso.noUsuario} - ${aviso.nombreUsuarioTitular}. Se oculta del historial, pero se puede consultar con "Mostrar canceladas".`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, cancelar',
      cancelButtonText: 'No'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.avisoAdeudoService.cancelar(aviso.avisoAdeudoId).subscribe({
        next: () => {
          this.openSnackBar('Aviso cancelado', 'Éxito');
          this.cargarHistorial();
        },
        error: (e: any) => {
          console.error(e);
          this.openSnackBar('No se pudo cancelar el aviso', 'Error');
        }
      });
    });
  }

  reactivarAviso(aviso: AvisoAdeudoModel): void {
    this.avisoAdeudoService.reactivar(aviso.avisoAdeudoId).subscribe({
      next: () => {
        this.openSnackBar('Aviso reactivado', 'Éxito');
        this.cargarHistorial();
      },
      error: (e: any) => {
        console.error(e);
        this.openSnackBar('No se pudo reactivar el aviso', 'Error');
      }
    });
  }

  applyCasaFilter(event: Event): void {
    this.casaFiltro = (event.target as HTMLInputElement).value;
    this.applyFilters();
  }

  applyEstatusComiteFilter(): void {
    this.applyFilters();
  }

  private applyFilters(): void {
    this.dataSource.filter = JSON.stringify({
      casa: this.casaFiltro.trim(),
      estatus: this.estatusComiteFiltro
    });
  }

  // Texto para la columna "Último aviso" -- ayuda a llevar el control de
  // a quién ya le toca el Segundo aviso (su Primero ya se entregó y sigue
  // debiendo, por eso sigue en esta lista de candidatos).
  etiquetaUltimoAviso(row: AdeudoLuzUsuarioModel): string {
    // Compromiso de pago (convenio) -- se antepone/agrega al resto del
    // texto normal en vez de reemplazarlo, para no perder de vista si ya
    // se le entregó algún aviso antes del convenio.
    const compromiso = row.enConvenioVigente
      ? ` -- en convenio (compromiso ${this.formatearFechaNaive(row.fechaCompromisoConvenio)})`
      : '';

    if (!row.ultimoTipoAviso) {
      return 'Sin avisos previos' + compromiso;
    }
    const tipo = row.ultimoTipoAviso === 'SEGUNDO' ? 'Segundo aviso' : 'Primer aviso';
    if (!row.ultimoAvisoEntregado) {
      return `${tipo} (aún no entregado)` + compromiso;
    }
    const fecha = row.ultimoAvisoFechaEntrega ? new Date(row.ultimoAvisoFechaEntrega).toLocaleDateString('es-MX') : '';
    if (row.enConvenioVigente) {
      return `${tipo} entregado ${fecha}` + compromiso;
    }
    return row.requiereSegundoAviso
      ? `${tipo} entregado ${fecha} -- falta Segundo aviso`
      : `${tipo} entregado ${fecha}`;
  }

  // ============================================================
  // Selección múltiple (checkboxes) -- patrón estándar de Angular
  // Material con SelectionModel sobre los datos YA filtrados visibles,
  // para que "seleccionar todos" no marque de golpe filas ocultas por el
  // filtro de N° de casa.
  isAllSelected(): boolean {
    const visibles = this.dataSource.filteredData;
    return visibles.length > 0 && visibles.every(row => this.selection.isSelected(row));
  }

  masterToggle(): void {
    if (this.isAllSelected()) {
      this.dataSource.filteredData.forEach(row => this.selection.deselect(row));
    } else {
      this.dataSource.filteredData.forEach(row => this.selection.select(row));
    }
    this.actualizarTotalSeleccionado();
  }

  toggleFila(row: AdeudoLuzUsuarioModel): void {
    this.selection.toggle(row);
    this.actualizarTotalSeleccionado();
  }

  private actualizarTotalSeleccionado(): void {
    this.totalAdeudoSeleccionado = this.selection.selected.reduce((acc, r) => acc + (Number(r.adeudoTotal) || 0), 0);
  }

  // Agrega una persona sin usuario en el sistema a la lista que se genera
  // junto con los seleccionados de la tabla. Solo el nombre es obligatorio.
  agregarNoRegistrado(): void {
    const nombre = this.nuevoNoRegistradoNombre.trim();
    if (!nombre) {
      this.openSnackBar('Escribe el nombre de la persona', 'Atención');
      return;
    }
    this.noRegistrados.push({
      nombre,
      direccion: this.nuevoNoRegistradoDireccion.trim() || undefined
    });
    this.nuevoNoRegistradoNombre = '';
    this.nuevoNoRegistradoDireccion = '';
  }

  quitarNoRegistrado(index: number): void {
    this.noRegistrados.splice(index, 1);
  }

  generarCartas(): void {
    const totalAGenerar = this.selection.selected.length + this.noRegistrados.length;
    if (totalAGenerar === 0) {
      this.openSnackBar('Selecciona al menos un usuario, o agrega una persona no registrada', 'Atención');
      return;
    }

    if (!this.fechaPresentacion) {
      this.openSnackBar('Indica la fecha en que debe presentarse a pagar antes de generar', 'Atención');
      return;
    }

    const etiquetaTipo = this.tiposAviso.find(t => t.valor === this.tipoAviso)?.etiqueta ?? 'aviso';

    // Control de Primer/Segundo aviso: si va a generar un Primer aviso
    // pero alguno de los seleccionados YA tiene un Primer aviso entregado
    // (le toca el Segundo, no repetir el Primero), se lo advierte antes de
    // generar -- no bloquea, por si de verdad quiere reimprimir el Primero.
    let advertencia = '';
    if (this.tipoAviso === 'PRIMERO') {
      const yaRequierenSegundo = this.selection.selected.filter(r => r.requiereSegundoAviso);
      if (yaRequierenSegundo.length > 0) {
        const nombres = yaRequierenSegundo.map(r => `${r.noUsuario} - ${r.nombreCompleto}`).join(', ');
        advertencia = `<br><br><strong style="color:#c62828">Atención:</strong> ${nombres} ya recibieron su Primer aviso -- `
          + `según el control, les toca el Segundo aviso, no repetir el Primero.`;
      }
    }

    // Convenio vigente: si va a generar un Segundo aviso para alguien con
    // un convenio activo sin vencer, se lo advierte -- el sistema ya no lo
    // marca como "requiere Segundo aviso" mientras esté en ese plazo, pero
    // no bloquea la generación por si de verdad se quiere generar de todas
    // formas (ej. incumplió otra parte del convenio).
    if (this.tipoAviso === 'SEGUNDO') {
      const enConvenio = this.selection.selected.filter(r => r.enConvenioVigente);
      if (enConvenio.length > 0) {
        const nombres = enConvenio.map(r => `${r.noUsuario} - ${r.nombreCompleto} (compromiso ${this.formatearFechaNaive(r.fechaCompromisoConvenio)})`).join(', ');
        advertencia += `<br><br><strong style="color:#1565c0">Atención:</strong> ${nombres} tienen un convenio de pago vigente -- `
          + `revisa si de verdad quieres generarles el Segundo aviso de todas formas.`;
      }
    }

    Swal.fire({
      title: `¿Generar ${etiquetaTipo} para ${totalAGenerar} persona(s)?`,
      html: `Se les asigna folio consecutivo. Los usuarios registrados quedan en el historial; las personas no registradas no.${advertencia}`,
      icon: advertencia ? 'warning' : 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, generar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.confirmarGeneracion();
    });
  }

  private confirmarGeneracion(): void {
    this.generando = true;
    const ids = this.selection.selected.map(r => r.aguaUsuarioId);

    this.avisoAdeudoService.generar(ids, this.tipoAviso, this.fechaPresentacion!, this.noRegistrados).subscribe({
      next: (resp) => {
        this.generando = false;
        const blob = resp.body;
        if (!blob) {
          this.openSnackBar('No se recibió el PDF generado', 'Error');
          return;
        }
        this.descargarBlob(blob, `avisos_adeudo_${this.tipoAviso.toLowerCase()}.pdf`);

        const omitidosTexto = resp.headers.get('X-Usuarios-Omitidos');
        if (omitidosTexto) {
          this.openSnackBar(`Cartas generadas. Se omitieron los usuarios ${omitidosTexto} (ya no tienen adeudo de luz).`, 'Atención');
        } else {
          this.openSnackBar('Cartas generadas correctamente', 'Éxito');
        }

        this.selection.clear();
        this.actualizarTotalSeleccionado();
        this.noRegistrados = [];
        this.historial = [];
        this.cargarHistorial();
        this.recargarCandidatosActuales();
      },
      error: (e: any) => {
        this.generando = false;
        console.error(e);
        const mensaje = e?.headers?.get?.('X-Error-Message') || 'No se pudieron generar las cartas';
        this.openSnackBar(mensaje, 'Error');
      }
    });
  }

  // Botón "Generar Segundo aviso" directo desde una fila del historial (un
  // Primer aviso ya entregado) -- evita tener que ir a la pantalla de
  // candidatos, buscar la calle/usuario de nuevo y volver a seleccionarlo.
  // Nunca se confía en lo que diga esta fila del historial (puede ser
  // vieja): siempre se vuelve a calcular su estado actual en el backend
  // antes de generar nada.
  generarSegundoDesdeAviso(aviso: AvisoAdeudoModel): void {
    if (!aviso.aguaUsuarioId) {
      this.openSnackBar('Este aviso no tiene un usuario vinculado (revisa si se generó antes de recompilar el backend) -- no se puede generar el Segundo desde aquí', 'Atención');
      return;
    }

    this.avisoAdeudoService.getCandidatoUnico(aviso.aguaUsuarioId).subscribe({
      next: (resp) => {
        const candidato: AdeudoLuzUsuarioModel | undefined = resp?.data?.[0];
        if (!candidato || !candidato.adeudoTotal || candidato.adeudoTotal <= 0) {
          Swal.fire('Ya no tiene adeudo', 'Este usuario ya no tiene adeudo de luz pendiente -- no aplica generarle el Segundo aviso.', 'info');
          return;
        }
        if (!candidato.requiereSegundoAviso) {
          if (candidato.enConvenioVigente) {
            Swal.fire(
              'Tiene un convenio vigente',
              `Este usuario tiene un convenio de pago vigente hasta el ${this.formatearFechaNaive(candidato.fechaCompromisoConvenio)} -- `
                + 'no procede el Segundo aviso mientras siga vigente.',
              'info'
            );
          } else {
            Swal.fire(
              'No aplica todavía',
              'Según el control de avisos, no le toca el Segundo aviso en este momento (puede que ya se le haya generado uno más reciente, o que este Primero ya no sea el más reciente).',
              'info'
            );
          }
          return;
        }
        this.pedirFechaYGenerarSegundo(candidato);
      },
      error: () => this.openSnackBar('No se pudo consultar el estado actual del usuario', 'Error')
    });
  }

  private pedirFechaYGenerarSegundo(candidato: AdeudoLuzUsuarioModel): void {
    Swal.fire({
      title: `¿Generar Segundo aviso para ${candidato.noUsuario} - ${candidato.nombreCompleto}?`,
      html: 'Indica la fecha en la que debe presentarse en el Comité:',
      input: 'date',
      inputValue: this.fechaPresentacion || '',
      showCancelButton: true,
      confirmButtonText: 'Generar',
      cancelButtonText: 'Cancelar',
      inputValidator: (value) => (!value ? 'Indica una fecha' : undefined)
    }).then(result => {
      if (!result.isConfirmed || !result.value) return;

      this.generando = true;
      this.avisoAdeudoService.generar([candidato.aguaUsuarioId], 'SEGUNDO', result.value, []).subscribe({
        next: (resp) => {
          this.generando = false;
          const blob = resp.body;
          if (!blob) {
            this.openSnackBar('No se recibió el PDF generado', 'Error');
            return;
          }
          this.descargarBlob(blob, `segundo_aviso_${candidato.noUsuario}.pdf`);
          this.openSnackBar('Segundo aviso generado correctamente', 'Éxito');
          this.historial = [];
          this.cargarHistorial();
          this.recargarCandidatosActuales();
        },
        error: (e: any) => {
          this.generando = false;
          const mensaje = e?.headers?.get?.('X-Error-Message') || 'No se pudo generar el Segundo aviso';
          this.openSnackBar(mensaje, 'Error');
        }
      });
    });
  }

  // Generar Segundo aviso para todos los seleccionados con el checkbox del
  // historial de una sola vez -- misma validación que el botón individual
  // (getCandidatoUnico por cada uno, nunca se confía en lo que diga la fila
  // del historial), pero se pide la fecha de presentación una sola vez y se
  // generan todos juntos en un único PDF.
  generarSegundosSeleccionados(): void {
    const seleccionados = this.historial.filter(a => this.avisosSeleccionadosSegundo.has(a.avisoAdeudoId));
    if (seleccionados.length === 0) return;

    const conUsuario = seleccionados.filter(a => !!a.aguaUsuarioId);
    const sinUsuario = seleccionados.filter(a => !a.aguaUsuarioId);
    if (sinUsuario.length > 0) {
      this.openSnackBar(`${sinUsuario.length} aviso(s) sin usuario vinculado se omitieron (se generaron antes de recompilar el backend)`, 'Atención');
    }
    if (conUsuario.length === 0) return;

    this.generando = true;
    const consultas = conUsuario.map(a => this.avisoAdeudoService.getCandidatoUnico(a.aguaUsuarioId!));
    forkJoin(consultas).subscribe({
      next: (resps: any[]) => {
        this.generando = false;
        const candidatos: AdeudoLuzUsuarioModel[] = [];
        const omitidos: string[] = [];

        resps.forEach((resp: any, i: number) => {
          const candidato: AdeudoLuzUsuarioModel | undefined = resp?.data?.[0];
          const aviso = conUsuario[i];
          if (!candidato || !candidato.adeudoTotal || candidato.adeudoTotal <= 0) {
            omitidos.push(`${aviso.noUsuario} (ya no tiene adeudo)`);
          } else if (!candidato.requiereSegundoAviso) {
            omitidos.push(`${aviso.noUsuario} (${candidato.enConvenioVigente ? 'tiene convenio vigente' : 'no le toca todavía'})`);
          } else {
            candidatos.push(candidato);
          }
        });

        if (candidatos.length === 0) {
          Swal.fire(
            'Nada que generar',
            'Ninguno de los seleccionados sigue aplicando para el Segundo aviso en este momento.'
              + (omitidos.length ? `<br><br>${omitidos.join('<br>')}` : ''),
            'info'
          );
          return;
        }

        this.pedirFechaYGenerarSegundos(candidatos, omitidos);
      },
      error: () => {
        this.generando = false;
        this.openSnackBar('No se pudo validar el estado actual de los usuarios seleccionados', 'Error');
      }
    });
  }

  private pedirFechaYGenerarSegundos(candidatos: AdeudoLuzUsuarioModel[], omitidos: string[]): void {
    const listaNombres = candidatos.map(c => `${c.noUsuario} - ${c.nombreCompleto}`).join(', ');
    Swal.fire({
      title: `¿Generar Segundo aviso para ${candidatos.length} usuario(s)?`,
      html: `${listaNombres}<br><br>Indica la fecha en la que deben presentarse en el Comité:`
        + (omitidos.length ? `<br><br><strong style="color:#c62828">Se omiten:</strong> ${omitidos.join(', ')}` : ''),
      input: 'date',
      inputValue: this.fechaPresentacion || '',
      showCancelButton: true,
      confirmButtonText: 'Generar',
      cancelButtonText: 'Cancelar',
      inputValidator: (value) => (!value ? 'Indica una fecha' : undefined)
    }).then(result => {
      if (!result.isConfirmed || !result.value) return;

      this.generando = true;
      const ids = candidatos.map(c => c.aguaUsuarioId);
      this.avisoAdeudoService.generar(ids, 'SEGUNDO', result.value, []).subscribe({
        next: (resp) => {
          this.generando = false;
          const blob = resp.body;
          if (!blob) {
            this.openSnackBar('No se recibió el PDF generado', 'Error');
            return;
          }
          this.descargarBlob(blob, `segundos_avisos_${ids.length}.pdf`);
          this.openSnackBar('Segundos avisos generados correctamente', 'Éxito');
          this.avisosSeleccionadosSegundo.clear();
          this.historial = [];
          this.cargarHistorial();
          this.recargarCandidatosActuales();
        },
        error: (e: any) => {
          this.generando = false;
          const mensaje = e?.headers?.get?.('X-Error-Message') || 'No se pudo generar el Segundo aviso';
          this.openSnackBar(mensaje, 'Error');
        }
      });
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
