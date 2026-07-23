import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import Swal from 'sweetalert2';
import { WaterEgresoLineaModel, WaterEgresoModel } from 'src/app/modules/shared/models/WaterEgreso.model';
import { WaterEgresoService } from 'src/app/modules/shared/services/water-egreso.service';
import { NewEgresoComponent } from '../../components/new-egreso/new-egreso.component';
import { NewGastoComponent } from '../../components/new-gasto/new-gasto.component';

const NOMBRES_MES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
];

// Los montos armados a mano en HTML (fuera de un pipe de Angular) necesitan
// su propio formateo con separador de miles; `toFixed(2)` no lo agrega.
function formatMonto(monto: number): string {
  return Number(monto).toLocaleString('es-MX', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// Lista plana de vales/recibos ya emitidos -- el "documento" tal cual, con
// su folio y su desglose completo. Complementa (no reemplaza) el acordeón
// de Egresos, que sirve para revisar por categoría/año/mes/proveedor pero
// no muestra el vale como unidad (sobre todo cuando mezcla varias
// categorías, como un vale de caja que junta nómina + una reparación).
@Component({
  selector: 'app-egreso-vales',
  templateUrl: './egreso-vales.component.html',
  styleUrls: ['./egreso-vales.component.css']
})
export class EgresoValesComponent implements OnInit {

  private readonly egresoService = inject(WaterEgresoService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly sanitizer = inject(DomSanitizer);
  public dialog = inject(MatDialog);

  displayColumns: string[] = ['fechaPagoStr', 'noFolio', 'tipoComprobanteNombre', 'categorias', 'proveedores', 'monto', 'revisado', 'validadoFisico', 'acciones'];
  dataSource = new MatTableDataSource<WaterEgresoModel>();

  // Vale actualmente expandido en la tabla (detalle inline, ya no en
  // ventana emergente). Solo uno a la vez.
  expandedElement: WaterEgresoModel | null = null;

  vales: WaterEgresoModel[] = [];

  // Control de folios: los folios de vale son una sola secuencia general
  // (no por año ni por categoría). Se recalculan cada vez que se recarga
  // la lista.
  folioMin = 0;
  folioMax = 0;
  folioGaps: number[] = [];
  folioDuplicados: { folio: string; vales: WaterEgresoModel[] }[] = [];
  mostrarControlFolios = false;

  // Filtros de limpieza histórica: "todos" no filtra por ese campo.
  filtroRevisado: 'todos' | 'si' | 'no' = 'todos';
  filtroValidadoFisico: 'todos' | 'si' | 'no' = 'todos';

  // Filtro por período (año/mes de fecha de pago del vale), para ir
  // depurando mes por mes. "todos" no filtra por ese campo.
  filtroAnio: number | 'todos' = 'todos';
  filtroMes: number | 'todos' = 'todos';
  aniosDisponibles: number[] = [];
  readonly meses = NOMBRES_MES;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.getVales();
  }

  getVales(): void {
    this.egresoService.getAll().subscribe({
      next: (resp: any) => this.processResponse(resp),
      error: (e: any) => console.error(e)
    });
  }

  private processResponse(resp: any): void {
    if (resp.metadata?.[0]?.code !== '00') return;
    const data: WaterEgresoModel[] = resp.data || [];
    // Orden ascendente por fecha de pago. Las fechas vienen "YYYY-MM-DD", así
    // que se comparan como texto directo, sin pasar por Date (mismo motivo
    // que en el acordeón de Egresos: evitar líos de zona horaria).
    data.sort((a, b) => (a.fechaPago || '').localeCompare(b.fechaPago || ''));
    this.vales = data;
    this.dataSource = new MatTableDataSource<WaterEgresoModel>(data);
    this.dataSource.paginator = this.paginator;
    this.expandedElement = null;
    this.construirControlFolios(data);
    this.aniosDisponibles = Array.from(new Set(
      data.map(v => this.anioMes(v.fechaPago).anio).filter(a => !isNaN(a))
    )).sort((a, b) => b - a);
    this.aplicarFiltros();
  }

  toggleDetalle(vale: WaterEgresoModel): void {
    this.expandedElement = this.expandedElement === vale ? null : vale;
  }

  // OJO: nunca usar `new Date(fecha)` para sacar año/mes -- ver el mismo
  // comentario en egreso-list.component.ts (líos de zona horaria con
  // fechas del día 1).
  private anioMes(fecha: string): { anio: number; mes: number } {
    const [anioStr, mesStr] = (fecha || '').split('-');
    return { anio: Number(anioStr), mes: Number(mesStr) - 1 };
  }

  // --- Filtros revisado / validado físico / período -----------------------

  aplicarFiltros(): void {
    const cumple = (valor: boolean | undefined, filtro: 'todos' | 'si' | 'no'): boolean => {
      if (filtro === 'todos') return true;
      return filtro === 'si' ? !!valor : !valor;
    };
    const filtrados = this.vales.filter(v => {
      const { anio, mes } = this.anioMes(v.fechaPago);
      const cumpleAnio = this.filtroAnio === 'todos' || anio === this.filtroAnio;
      const cumpleMes = this.filtroMes === 'todos' || mes === this.filtroMes;
      return cumple(v.revisado, this.filtroRevisado)
        && cumple(v.validadoFisico, this.filtroValidadoFisico)
        && cumpleAnio && cumpleMes;
    });
    this.dataSource.data = filtrados;
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  toggleRevisado(vale: WaterEgresoModel): void {
    const nuevoValor = !vale.revisado;
    this.egresoService.marcar(vale.aguaEgresosId!, { revisado: nuevoValor }).subscribe({
      next: () => {
        vale.revisado = nuevoValor;
        this.aplicarFiltros();
      },
      error: () => this.openSnackBar('Error al actualizar "revisado"', 'Error')
    });
  }

  toggleValidadoFisico(vale: WaterEgresoModel): void {
    const nuevoValor = !vale.validadoFisico;
    this.egresoService.marcar(vale.aguaEgresosId!, { validadoFisico: nuevoValor }).subscribe({
      next: () => {
        vale.validadoFisico = nuevoValor;
        this.aplicarFiltros();
      },
      error: () => this.openSnackBar('Error al actualizar "validado en físico"', 'Error')
    });
  }

  // --- Control de folios --------------------------------------------------

  private folioNumerico(folio: string | undefined | null): number | null {
    if (!folio) return null;
    const n = parseInt(folio, 10);
    return isNaN(n) ? null : n;
  }

  private formatearFolio(numero: number): string {
    return String(numero).padStart(4, '0');
  }

  private construirControlFolios(vales: WaterEgresoModel[]): void {
    const conNumero = vales
      .map(vale => ({ vale, numero: this.folioNumerico(vale.noFolio) }))
      .filter((x): x is { vale: WaterEgresoModel; numero: number } => x.numero !== null);

    if (conNumero.length === 0) {
      this.folioMin = 0;
      this.folioMax = 0;
      this.folioGaps = [];
      this.folioDuplicados = [];
      return;
    }

    const numeros = conNumero.map(x => x.numero);
    this.folioMin = Math.min(...numeros);
    this.folioMax = Math.max(...numeros);

    const porNumero = new Map<number, WaterEgresoModel[]>();
    conNumero.forEach(x => {
      if (!porNumero.has(x.numero)) porNumero.set(x.numero, []);
      porNumero.get(x.numero)!.push(x.vale);
    });

    this.folioDuplicados = Array.from(porNumero.entries())
      .filter(([, valesConMismoFolio]) => valesConMismoFolio.length > 1)
      .map(([numero, valesConMismoFolio]) => ({ folio: this.formatearFolio(numero), vales: valesConMismoFolio }))
      .sort((a, b) => a.folio.localeCompare(b.folio));

    const gaps: number[] = [];
    for (let n = this.folioMin; n <= this.folioMax; n++) {
      if (!porNumero.has(n)) gaps.push(n);
    }
    this.folioGaps = gaps;
  }

  folioFaltante(numero: number): string {
    return this.formatearFolio(numero);
  }

  get siguienteFolioSugerido(): string {
    return this.formatearFolio((this.folioMax || 0) + 1);
  }

  // Un vale histórico (sin líneas) trae su categoría directo en la
  // cabecera; uno nuevo puede tener una o varias categorías en sus líneas.
  categorias(vale: WaterEgresoModel): string {
    if (!vale.lineas || vale.lineas.length === 0) {
      return vale.conceptoNombre || 'Sin categoría';
    }
    const nombres = Array.from(new Set(vale.lineas.map(l => l.conceptoNombre || 'Sin categoría')));
    return nombres.length > 2 ? `${nombres.length} categorías` : nombres.join(', ');
  }

  proveedores(vale: WaterEgresoModel): string {
    if (!vale.lineas || vale.lineas.length === 0) {
      return vale.proveedor || vale.personaNombre || '-';
    }
    const nombres = Array.from(new Set(
      vale.lineas.map(l => l.proveedor || l.personaNombre).filter(Boolean) as string[]
    ));
    if (nombres.length === 0) return vale.proveedor || vale.personaNombre || '-';
    return nombres.length > 1 ? 'Varios' : nombres[0];
  }

  openNewEgresoDialog(): void {
    const dialogRef = this.dialog.open(NewEgresoComponent, {
      width: '900px',
      data: { folioSugerido: this.siguienteFolioSugerido }
    });
    dialogRef.afterClosed().subscribe((result: any) => {
      if (result === 1) {
        this.openSnackBar('Vale registrado', 'Éxito');
        this.getVales();
      } else if (result === 2) {
        this.openSnackBar('Error al guardar el vale', 'Error');
      }
    });
  }

  // Captura un gasto suelto (sin vale todavía) directo desde Vales, para no
  // tener que ir a "Gastos del mes" solo para esto. Ese gasto se queda
  // pendiente hasta que se junte con otros en un vale (desde Gastos del mes,
  // seleccionando por proveedor/categoría y emitiendo).
  openNewGastoDialog(): void {
    const dialogRef = this.dialog.open(NewGastoComponent, { width: '700px' });
    dialogRef.afterClosed().subscribe((result: any) => {
      if (result === 1) {
        this.openSnackBar('Gasto registrado (pendiente de incluir en un vale)', 'Éxito');
      } else if (result === 2) {
        this.openSnackBar('Error al guardar el gasto', 'Error');
      }
    });
  }

  // Renderiza una línea y, si trae sublineas (dato histórico: un mismo
  // comprobante desglosado en varias sub-compras), las renderiza debajo,
  // indentadas y en gris -- son detalle informativo, nunca se vuelven a
  // sumar aparte del monto de la línea que las contiene.
  private filaLineaHtml(l: WaterEgresoLineaModel, folioVale: string | undefined, indent: number): string {
    const folio = l.noFolio || (indent === 0 ? folioVale : undefined) || '-';
    const categoria = (indent > 0 ? '&nbsp;'.repeat(indent * 4) + '↳ ' : '') + (l.conceptoNombre || '-');
    const proveedor = l.proveedor || l.personaNombre || '-';
    const colorTexto = indent > 0 ? 'color:#777;font-style:italic;' : '';
    const fondo = indent > 0 ? 'background:#fafafa;' : '';
    const celda = `padding:6px 10px;border-bottom:1px solid #eee;${colorTexto}${fondo}`;
    const fila = `<tr>
        <td style="text-align:left;${celda}">${folio}</td>
        <td style="text-align:left;${celda}">${categoria}</td>
        <td style="text-align:left;${celda}">${proveedor}</td>
        <td style="text-align:left;white-space:pre-line;${celda}">${l.descripcion || '-'}</td>
        <td style="text-align:left;${celda}">${l.fechaPagoStr || ''}</td>
        <td style="text-align:right;${celda}">$${formatMonto(l.monto)}</td>
      </tr>`;
    const subFilas = (l.sublineas && l.sublineas.length > 0)
      ? l.sublineas.map(sub => this.filaLineaHtml(sub, folioVale, indent + 1)).join('')
      : '';
    return fila + subFilas;
  }

  // Cada fila de este detalle es un recibo/comprobante que debe existir en
  // físico: si la línea trae su propio folio (compra individual dentro del
  // vale) se muestra ese; si no, se asume que comparte el folio del vale.
  // Ahora se muestra inline (fila expandible), ya no en ventana emergente.
  detalleHtml(vale: WaterEgresoModel): string {
    const filas = (vale.lineas && vale.lineas.length > 0)
      ? vale.lineas.map(l => this.filaLineaHtml(l, vale.noFolio, 0)).join('')
      : this.filaLineaHtml({
          conceptoNombre: vale.conceptoNombre,
          proveedor: vale.proveedor,
          personaNombre: vale.personaNombre,
          descripcion: vale.descripcion,
          fechaPagoStr: vale.fechaPagoStr,
          monto: vale.monto,
          noFolio: vale.noFolio,
          conceptoId: vale.conceptoId || 0
        }, vale.noFolio, 0);

    const cabeceraCelda = 'text-align:left;padding:6px 10px;border-bottom:2px solid #ccc;';
    // Si el vale tiene varias líneas, la descripción de la cabecera es una
    // nota general aparte (no se repite con la de cada línea).
    const notaCabecera = (vale.lineas && vale.lineas.length > 0 && vale.descripcion)
      ? `<p style="text-align:left;white-space:pre-line;margin:0 0 8px"><strong>Nota del vale:</strong> ${vale.descripcion}</p>`
      : '';
    return `
      ${notaCabecera}
      <table style="width:100%;border-collapse:collapse;font-size:0.9em">
        <thead>
          <tr>
            <th style="${cabeceraCelda}">Folio</th>
            <th style="${cabeceraCelda}">Categoría</th>
            <th style="${cabeceraCelda}">Proveedor</th>
            <th style="${cabeceraCelda}">Descripción</th>
            <th style="${cabeceraCelda}">Fecha</th>
            <th style="${cabeceraCelda}text-align:right">Monto</th>
          </tr>
        </thead>
        <tbody>${filas}</tbody>
      </table>
    `;
  }

  // El HTML se genera enteramente desde nuestros propios datos (nunca texto
  // libre de un tercero sin escapar), por eso se marca como seguro en vez de
  // dejar que Angular lo sanitice y potencialmente recorte los estilos.
  detalleSafeHtml(vale: WaterEgresoModel): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(this.detalleHtml(vale));
  }

  eliminar(vale: WaterEgresoModel): void {
    Swal.fire({
      title: '¿Desactivar este vale?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, desactivar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed || !vale.aguaEgresosId) return;
      this.egresoService.deactivate(vale.aguaEgresosId).subscribe({
        next: () => {
          this.openSnackBar('Vale desactivado', 'Éxito');
          this.getVales();
        },
        error: () => this.openSnackBar('Error al desactivar el vale', 'Error')
      });
    });
  }

  openSnackBar(message: string, action: string): MatSnackBarRef<SimpleSnackBar> {
    return this.snackBar.open(message, action, { duration: 2000 });
  }
}
