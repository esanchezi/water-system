import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { Router } from '@angular/router';
import { SelectionModel } from '@angular/cdk/collections';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import Swal from 'sweetalert2';
import { AvisoPadronService } from '../../../shared/services/aviso-padron.service';
import { CatalogService } from '../../../shared/services/catalog.service';
import { CatalogOptionModel } from '../../../shared/models/Catalog.model';
import { TIPOS_ENTREGA } from '../../../shared/models/AvisoAdeudo.model';
import { AvisoPadronCandidatoModel, AvisoPadronModel } from '../../../shared/models/AvisoPadron.model';
import { AvisoPadronEntregaDialogComponent } from '../../components/aviso-padron-entrega-dialog/aviso-padron-entrega-dialog.component';

// Mismo id de catálogo de calles que ya usa Cartas de adeudo / Aviso de
// bomba / Usuarios para la cascada Zona (Sección) -> Calle.
const CATALOGO_CALLES_ID = 15;

const ESTATUS_DADO_DE_BAJA_ID = 102;

// Control de "Avisos para actualización del padrón de habitantes" (Art. 5,
// 15, 15 Bis y 15 Ter) -- igual que Aviso de bomba, aquí NO hay cálculo de
// deuda: cualquier usuario activo de la calle aplica por igual. Por eso la
// pantalla es igual de simple: elegir zona/calle, seleccionar usuarios,
// indicar la fecha en que deben presentarse y generar.
@Component({
  selector: 'app-aviso-padron-list',
  templateUrl: './aviso-padron-list.component.html',
  styleUrls: ['./aviso-padron-list.component.css']
})
export class AvisoPadronListComponent implements OnInit {

  private readonly avisoPadronService = inject(AvisoPadronService);
  private readonly catalogService = inject(CatalogService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  displayColumns: string[] = [
    'select', 'calleNombre', 'casaNo', 'noUsuario', 'nombreCompleto', 'estatusComiteNombre'
  ];

  historialColumns: string[] = [
    'folioNotificacion', 'noUsuario', 'nombreUsuarioTitular', 'noCasaTexto', 'domicilioToma',
    'fechaPresentacion', 'motivoSolicitud', 'dateAdd', 'entrega', 'acciones'
  ];

  tiposEntrega = TIPOS_ENTREGA;

  dataSource = new MatTableDataSource<AvisoPadronCandidatoModel>();
  selection = new SelectionModel<AvisoPadronCandidatoModel>(true, []);

  // Fecha en la que debe presentarse -- se pide siempre antes de generar,
  // se llena en la carta ("Debe presentarse el día ___ de ___ de 20__").
  fechaPresentacion: string | null = null;

  // Motivo por el que se solicita la actualización -- opcional, se imprime
  // en la carta cuando se captura (pedido de Ely: quiere dejar constancia
  // de la razón concreta de cada solicitud).
  motivoSolicitud = '';

  zonas: CatalogOptionModel[] = [];
  private todasLasCalles: CatalogOptionModel[] = [];
  calles: CatalogOptionModel[] = [];
  zonaIdSeleccionada: number | null = null;
  calleIdSeleccionada: number | null = null;

  casaFiltro = '';
  estatusComiteFiltro = '';
  estatusComiteOpciones: string[] = [];

  cargando = false;
  generando = false;
  yaSeBusco = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  historial: AvisoPadronModel[] = [];
  cargandoHistorial = false;
  mostrarCanceladas = false;

  get historialVisible(): AvisoPadronModel[] {
    return this.mostrarCanceladas ? this.historial : this.historial.filter(a => !a.cancelado);
  }

  etiquetaTipoEntrega(valor: string | undefined): string {
    return this.tiposEntrega.find(t => t.valor === valor)?.etiqueta ?? valor ?? '';
  }

  // "dd/MM/yyyy" armado con puro texto -- ver mismo criterio en
  // AvisoAdeudoListComponent.formatearFechaNaive().
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
    if (calleId != null) {
      this.cargarCandidatos(calleId);
    } else {
      this.limpiarResultados();
    }
  }

  private limpiarResultados(): void {
    this.yaSeBusco = false;
    this.dataSource = new MatTableDataSource<AvisoPadronCandidatoModel>();
    this.estatusComiteFiltro = '';
    this.estatusComiteOpciones = [];
    this.selection.clear();
  }

  private cargarCandidatos(calleId: number): void {
    this.cargando = true;
    this.yaSeBusco = true;
    this.selection.clear();
    this.avisoPadronService.getCandidatosPorCalle(calleId).subscribe({
      next: (resp: any) => {
        this.cargando = false;
        if (resp.metadata?.code === '00') {
          const data: AvisoPadronCandidatoModel[] = resp.data || [];
          this.dataSource = new MatTableDataSource<AvisoPadronCandidatoModel>(data);
          this.dataSource.paginator = this.paginator;
          this.dataSource.filterPredicate = (row: AvisoPadronCandidatoModel, filter: string) => {
            const f = JSON.parse(filter);
            const matchCasa = !f.casa || String(row.casaNo ?? '').includes(f.casa);
            const matchEstatus = f.estatus
              ? row.estatusComiteNombre === f.estatus
              : row.estatusComiteId !== ESTATUS_DADO_DE_BAJA_ID;
            return matchCasa && matchEstatus;
          };

          this.estatusComiteOpciones = [...new Set(
            data.map(d => d.estatusComiteNombre).filter((v): v is string => !!v)
          )].sort();

          this.applyFilters();
        } else {
          this.openSnackBar(resp?.metadata?.message || 'No se pudieron cargar los usuarios de esta calle', 'Error');
        }
      },
      error: (e: any) => {
        this.cargando = false;
        console.error(e);
        this.openSnackBar('No se pudieron cargar los usuarios de esta calle', 'Error');
      }
    });
  }

  private recargarCandidatosActuales(): void {
    if (this.calleIdSeleccionada != null) {
      this.cargarCandidatos(this.calleIdSeleccionada);
    }
  }

  private cargarHistorial(): void {
    this.cargandoHistorial = true;
    this.avisoPadronService.getHistorial().subscribe({
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
  }

  toggleFila(row: AvisoPadronCandidatoModel): void {
    this.selection.toggle(row);
  }

  // Link directo a la ficha completa del usuario -- se abre en pestaña nueva
  // (mismo criterio que Cartas de adeudo) para no perder el filtro/selección
  // en curso en esta pantalla.
  irADetalleUsuario(aguaUsuarioId?: number): void {
    if (!aguaUsuarioId) return;
    const urlTree = this.router.createUrlTree(['dashboard/detailsUser'], {
      queryParams: { element: JSON.stringify({ usuarioId: aguaUsuarioId }) }
    });
    window.open(this.router.serializeUrl(urlTree), '_blank');
  }

  generarAvisos(): void {
    if (this.selection.selected.length === 0) {
      this.openSnackBar('Selecciona al menos un usuario', 'Atención');
      return;
    }
    if (!this.fechaPresentacion) {
      this.openSnackBar('Indica la fecha en que debe presentarse antes de generar', 'Atención');
      return;
    }

    const total = this.selection.selected.length;
    Swal.fire({
      title: `¿Generar Aviso de actualización de padrón para ${total} persona(s)?`,
      html: 'Se les asigna folio consecutivo y queda registrado en el historial.',
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
    this.generando = true;
    const ids = this.selection.selected.map(r => r.aguaUsuarioId);

    this.avisoPadronService.generar(ids, this.fechaPresentacion!, this.motivoSolicitud.trim() || undefined).subscribe({
      next: (resp) => {
        this.generando = false;
        const blob = resp.body;
        if (!blob) {
          this.openSnackBar('No se recibió el PDF generado', 'Error');
          return;
        }
        this.descargarBlob(blob, 'avisos_padron.pdf');
        this.openSnackBar('Avisos generados correctamente', 'Éxito');

        this.selection.clear();
        this.motivoSolicitud = '';
        this.historial = [];
        this.cargarHistorial();
        this.recargarCandidatosActuales();
      },
      error: (e: any) => {
        this.generando = false;
        console.error(e);
        const mensaje = e?.headers?.get?.('X-Error-Message') || 'No se pudieron generar los avisos';
        this.openSnackBar(mensaje, 'Error');
      }
    });
  }

  registrarEntrega(aviso: AvisoPadronModel): void {
    const dialogRef = this.dialog.open(AvisoPadronEntregaDialogComponent, {
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

  // Reabre el mismo dialog solo para consultar/agregar/quitar las fotos de
  // respaldo de una entrega ya registrada -- ver AvisoAdeudoListComponent,
  // mismo patrón.
  verEvidenciaFotografica(aviso: AvisoPadronModel): void {
    this.dialog.open(AvisoPadronEntregaDialogComponent, {
      width: '480px',
      data: { aviso, soloVerFotos: true }
    });
  }

  cancelarAviso(aviso: AvisoPadronModel): void {
    Swal.fire({
      title: `¿Cancelar el folio ${aviso.folioNotificacion}?`,
      text: `${aviso.noUsuario} - ${aviso.nombreUsuarioTitular}. Se oculta del historial, pero se puede consultar con "Mostrar canceladas".`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, cancelar',
      cancelButtonText: 'No'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.avisoPadronService.cancelar(aviso.avisoPadronId).subscribe({
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

  reactivarAviso(aviso: AvisoPadronModel): void {
    this.avisoPadronService.reactivar(aviso.avisoPadronId).subscribe({
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
