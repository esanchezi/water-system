import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { Router } from '@angular/router';
import { SelectionModel } from '@angular/cdk/collections';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import Swal from 'sweetalert2';
import { AvisoBombaService } from '../../../shared/services/aviso-bomba.service';
import { CatalogService } from '../../../shared/services/catalog.service';
import { CatalogOptionModel } from '../../../shared/models/Catalog.model';
import { TIPOS_ENTREGA } from '../../../shared/models/AvisoAdeudo.model';
import { AvisoBombaCandidatoModel, AvisoBombaModel } from '../../../shared/models/AvisoBomba.model';
import { AvisoBombaEntregaDialogComponent } from '../../components/aviso-bomba-entrega-dialog/aviso-bomba-entrega-dialog.component';

// Mismo id de catálogo de calles que ya usa Cartas de adeudo / Usuarios
// para la cascada Zona (Sección) -> Calle.
const CATALOGO_CALLES_ID = 15;

const ESTATUS_DADO_DE_BAJA_ID = 102;

// Control de "Avisos por uso indebido de bomba" (Art. 23) -- a diferencia
// de Cartas de adeudo, aquí NO hay cálculo de deuda: cualquier usuario
// activo de la calle aplica por igual (el aviso se emite por un reporte de
// uso de bomba, no por adeudo). Por eso la pantalla es más simple: elegir
// zona/calle, seleccionar usuarios, indicar la fecha del reporte y generar.
@Component({
  selector: 'app-aviso-bomba-list',
  templateUrl: './aviso-bomba-list.component.html',
  styleUrls: ['./aviso-bomba-list.component.css']
})
export class AvisoBombaListComponent implements OnInit {

  private readonly avisoBombaService = inject(AvisoBombaService);
  private readonly catalogService = inject(CatalogService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  displayColumns: string[] = [
    'select', 'calleNombre', 'casaNo', 'noUsuario', 'nombreCompleto', 'estatusComiteNombre'
  ];

  historialColumns: string[] = [
    'folioNotificacion', 'noUsuario', 'nombreUsuarioTitular', 'noCasaTexto', 'domicilioToma',
    'fechaReporte', 'dateAdd', 'entrega', 'acciones'
  ];

  tiposEntrega = TIPOS_ENTREGA;

  dataSource = new MatTableDataSource<AvisoBombaCandidatoModel>();
  selection = new SelectionModel<AvisoBombaCandidatoModel>(true, []);

  // Fecha del reporte / revisión que se llena en la carta -- se pide
  // siempre antes de generar.
  fechaReporte: string | null = null;

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

  historial: AvisoBombaModel[] = [];
  cargandoHistorial = false;
  mostrarCanceladas = false;

  get historialVisible(): AvisoBombaModel[] {
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
    this.dataSource = new MatTableDataSource<AvisoBombaCandidatoModel>();
    this.estatusComiteFiltro = '';
    this.estatusComiteOpciones = [];
    this.selection.clear();
  }

  private cargarCandidatos(calleId: number): void {
    this.cargando = true;
    this.yaSeBusco = true;
    this.selection.clear();
    this.avisoBombaService.getCandidatosPorCalle(calleId).subscribe({
      next: (resp: any) => {
        this.cargando = false;
        if (resp.metadata?.code === '00') {
          const data: AvisoBombaCandidatoModel[] = resp.data || [];
          this.dataSource = new MatTableDataSource<AvisoBombaCandidatoModel>(data);
          this.dataSource.paginator = this.paginator;
          this.dataSource.filterPredicate = (row: AvisoBombaCandidatoModel, filter: string) => {
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
    this.avisoBombaService.getHistorial().subscribe({
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

  toggleFila(row: AvisoBombaCandidatoModel): void {
    this.selection.toggle(row);
  }

  // Link directo a la ficha completa del usuario -- misma convención que ya
  // usan Personas, Deudores y Cartas de adeudo.
  irADetalleUsuario(aguaUsuarioId?: number): void {
    if (!aguaUsuarioId) return;
    this.router.navigate(['dashboard/detailsUser'], {
      queryParams: { element: JSON.stringify({ usuarioId: aguaUsuarioId }) }
    });
  }

  generarAvisos(): void {
    if (this.selection.selected.length === 0) {
      this.openSnackBar('Selecciona al menos un usuario', 'Atención');
      return;
    }
    if (!this.fechaReporte) {
      this.openSnackBar('Indica la fecha del reporte / revisión antes de generar', 'Atención');
      return;
    }

    const total = this.selection.selected.length;
    Swal.fire({
      title: `¿Generar Aviso por uso de bomba para ${total} persona(s)?`,
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

    this.avisoBombaService.generar(ids, this.fechaReporte!).subscribe({
      next: (resp) => {
        this.generando = false;
        const blob = resp.body;
        if (!blob) {
          this.openSnackBar('No se recibió el PDF generado', 'Error');
          return;
        }
        this.descargarBlob(blob, 'avisos_bomba.pdf');
        this.openSnackBar('Avisos generados correctamente', 'Éxito');

        this.selection.clear();
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

  registrarEntrega(aviso: AvisoBombaModel): void {
    const dialogRef = this.dialog.open(AvisoBombaEntregaDialogComponent, {
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
  verEvidenciaFotografica(aviso: AvisoBombaModel): void {
    this.dialog.open(AvisoBombaEntregaDialogComponent, {
      width: '480px',
      data: { aviso, soloVerFotos: true }
    });
  }

  cancelarAviso(aviso: AvisoBombaModel): void {
    Swal.fire({
      title: `¿Cancelar el folio ${aviso.folioNotificacion}?`,
      text: `${aviso.noUsuario} - ${aviso.nombreUsuarioTitular}. Se oculta del historial, pero se puede consultar con "Mostrar cancelados".`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, cancelar',
      cancelButtonText: 'No'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.avisoBombaService.cancelar(aviso.avisoBombaId).subscribe({
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

  reactivarAviso(aviso: AvisoBombaModel): void {
    this.avisoBombaService.reactivar(aviso.avisoBombaId).subscribe({
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
