import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { WaterReceiptModel, WaterReceiptPaymentModel } from 'src/app/modules/shared/models/WaterReceipt.model';
import { ReceiptService } from '../../../shared/services/receipt.service';
import { CatalogService } from '../../../shared/services/catalog.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import { NewReceiptComponent } from '../new-receipt/new-receipt.component';
import { ReceiptCancelledComponent } from '../receipt-cancelled/receipt-cancelled.component';

// Catálogo de "Tipo pago" (mismo id que usa la captura de recibo).
const CATALOGO_TIPO_PAGO_ID = 4;

// Listado de recibos: una fila por RECIBO (no una por línea de pago), con el
// detalle de su aplicación (concepto, año, montos) desplegable en la misma
// fila. Se carga por rango de fechas (por default, el año en curso) para no
// traer todo el historial de golpe; la búsqueda y los demás filtros se
// aplican sobre lo ya cargado.
@Component({
  selector: 'app-receipt',
  templateUrl: './receipt.component.html',
  styleUrls: ['./receipt.component.css']
})
export class ReceiptComponent implements OnInit {

  private receiptService = inject(ReceiptService);
  private catalogService = inject(CatalogService);
  private snackBar = inject(MatSnackBar);
  public dialog = inject(MatDialog);

  displayColumns: string[] = ['expand', 'noFolio', 'fecha', 'usuario', 'concepto', 'observaciones', 'total', 'actions'];
  dataSource = new MatTableDataSource<WaterReceiptModel>();

  // Recibo desplegado (detalle de su aplicación) -- uno a la vez.
  expandido: WaterReceiptModel | null = null;

  // Rango de fechas cargado desde el servidor (yyyy-MM-dd). Vacío = sin límite.
  desde = `${new Date().getFullYear()}-01-01`;
  hasta = new Date().toISOString().substring(0, 10);

  // Filtros sobre lo ya cargado.
  texto = '';
  conceptoFiltro = '';
  anioFiltro = '';
  tipoPagoFiltro = '';
  // Los folios cancelados se capturan como un recibo con concepto
  // "CANCELADO" y sin usuario ni pagos (ver "Agregar Recibo Cancelado"); por
  // default se ocultan para no mezclarlos con los cobros reales.
  mostrarCancelados = false;

  conceptosOpciones: string[] = [];
  aniosOpciones: number[] = [];
  tiposPago: { id: number; nombre: string }[] = [];
  private tipoPagoNombre = new Map<number, string>();

  cargando = false;

  @ViewChild(MatPaginator)
  paginator !: MatPaginator;

  ngOnInit(): void {
    this.dataSource.filterPredicate = (r: WaterReceiptModel, filter: string) => {
      const f = JSON.parse(filter);
      const pagos = r.waterReceiptPayment || [];

      if (!f.cancelados && this.esCancelado(r)) return false;

      // Búsqueda por folio, número de usuario, nombre, concepto u observaciones
      // (todas las palabras deben aparecer, en cualquier orden).
      const persona = r.waterUser?.person;
      const base = [
        r.noFolio, r.waterUser?.noUsuario,
        persona?.nombre, persona?.nombre2, persona?.app, persona?.apm,
        r.concepto, r.observaciones
      ].filter(v => v != null && v !== '').join(' ').toLowerCase();
      const palabras: string[] = f.texto ? f.texto.split(' ').filter((w: string) => w.length > 0) : [];
      const matchTexto = palabras.every(p => base.includes(p));

      const matchConcepto = !f.concepto || pagos.some(p => p.concepto === f.concepto);
      const matchAnio = !f.anio || pagos.some(p => String(p.anio) === f.anio);
      const matchTipoPago = !f.tipoPago || pagos.some(p => String(p.tipoPagoId) === f.tipoPago);
      return matchTexto && matchConcepto && matchAnio && matchTipoPago;
    };

    this.cargarTiposPago();
    this.getReceipt();
  }

  private cargarTiposPago(): void {
    this.catalogService.getOptions(CATALOGO_TIPO_PAGO_ID).subscribe({
      next: (opts) => {
        this.tiposPago = opts.map(o => ({ id: o.catalogoOpcionesId, nombre: o.nombre }));
        this.tipoPagoNombre = new Map(this.tiposPago.map(t => [t.id, t.nombre]));
      },
      error: (e: any) => console.error(e)
    });
  }

  nombreTipoPago(id?: number): string {
    return id != null ? (this.tipoPagoNombre.get(id) ?? '') : '';
  }

  getReceipt(): void {
    this.cargando = true;
    this.expandido = null;
    this.receiptService.getReceiptPorRango(this.desde, this.hasta)
      .subscribe({
        next: (v) => { this.cargando = false; this.processReceiptResponse(v); },
        error: (e) => {
          this.cargando = false;
          console.error(e);
          this.openSnackBar('No se pudieron cargar los recibos', 'Error');
        }
      });
  }

  // Botón "Todo": quita el límite de fechas.
  verTodo(): void {
    this.desde = '';
    this.hasta = '';
    this.getReceipt();
  }

  processReceiptResponse(resp: any) {
    if (resp.metadata?.[0]?.code !== '00') {
      this.dataSource.data = [];
      return;
    }
    const recibos: WaterReceiptModel[] = resp.data || [];
    this.dataSource.data = recibos;
    this.dataSource.paginator = this.paginator;

    this.conceptosOpciones = [...new Set(
      recibos.flatMap(r => (r.waterReceiptPayment || []).map(p => p.concepto)).filter((c): c is string => !!c)
    )].sort();
    this.aniosOpciones = [...new Set(
      recibos.flatMap(r => (r.waterReceiptPayment || []).map(p => p.anio)).filter((a): a is number => a != null)
    )].sort((a, b) => b - a);

    this.aplicarFiltros();
  }

  buscar(texto: string): void {
    this.texto = texto.trim().toLowerCase();
    this.aplicarFiltros();
  }

  aplicarFiltros(): void {
    this.dataSource.filter = JSON.stringify({
      texto: this.texto,
      concepto: this.conceptoFiltro,
      anio: this.anioFiltro,
      tipoPago: this.tipoPagoFiltro,
      cancelados: this.mostrarCancelados
    });
    this.paginator?.firstPage();
  }

  limpiarFiltros(input: HTMLInputElement): void {
    input.value = '';
    this.texto = '';
    this.conceptoFiltro = '';
    this.anioFiltro = '';
    this.tipoPagoFiltro = '';
    this.mostrarCancelados = false;
    this.aplicarFiltros();
  }

  esCancelado(r: WaterReceiptModel): boolean {
    return (r.concepto || '').trim().toUpperCase() === 'CANCELADO';
  }

  get totalCancelados(): number {
    return this.dataSource.data.filter(r => this.esCancelado(r)).length;
  }

  toggleDetalle(recibo: WaterReceiptModel): void {
    this.expandido = this.expandido === recibo ? null : recibo;
  }

  nombreCompleto(r: WaterReceiptModel): string {
    const p = r.waterUser?.person;
    return [p?.nombre, p?.nombre2, p?.app, p?.apm].filter(Boolean).join(' ');
  }

  // "dd/MM/yyyy" armado con texto (yyyy-MM-dd) para que la fecha no se corra
  // un día por zona horaria.
  formatearFecha(fecha?: string): string {
    if (!fecha) return '';
    const [anio, mes, dia] = fecha.substring(0, 10).split('-');
    return `${dia}/${mes}/${anio}`;
  }

  lineasDe(r: WaterReceiptModel): WaterReceiptPaymentModel[] {
    return r.waterReceiptPayment || [];
  }

  // Exporta a CSV (abre directo en Excel) lo que se ve con los filtros
  // actuales, una fila por línea de pago.
  exportExcel(): void {
    const filas: (string | number)[][] = [[
      'Folio', 'Fecha', 'N° Usuario', 'Usuario', 'Concepto recibo', 'Observaciones', 'Total recibo',
      'Concepto pago', 'Año', 'Tipo pago', 'Monto recibido', 'Monto aplicado'
    ]];
    this.dataSource.filteredData.forEach(r => {
      const base = [
        r.noFolio, this.formatearFecha(r.fecha), r.waterUser?.noUsuario ?? '', this.nombreCompleto(r),
        r.concepto ?? '', r.observaciones ?? '', r.total ?? ''
      ];
      const lineas = this.lineasDe(r);
      if (lineas.length === 0) {
        filas.push([...base, '', '', '', '', '']);
      } else {
        lineas.forEach(p => filas.push([
          ...base, p.concepto ?? '', p.anio ?? '', this.nombreTipoPago(p.tipoPagoId),
          p.montoRecibido ?? '', p.montoAplicado ?? ''
        ]));
      }
    });
    const escapar = (v: string | number) => `"${String(v).replace(/"/g, '""')}"`;
    const csv = '﻿' + filas.map(f => f.map(escapar).join(',')).join('\r\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = `recibos_${new Date().toISOString().substring(0, 10)}.csv`;
    enlace.click();
    URL.revokeObjectURL(url);
  }

  openReciboDialog() {
    const dialogRef = this.dialog.open(NewReceiptComponent, {
      width: '1200px'
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result == 1) {
        this.openSnackBar('Recibo agregado', 'Éxito');
        this.getReceipt();
      } else if (result == 2) {
        this.openSnackBar('Error al guardar el recibo', 'Error');
      }
    });
  }

  // Editar un recibo ya existente abre el mismo formulario de captura,
  // prellenado con todo (usuario, folio, montos aplicados, años, etc.).
  editarRecibo(recibo: WaterReceiptModel): void {
    const dialogRef = this.dialog.open(NewReceiptComponent, {
      width: '1200px',
      data: { receipt: recibo }
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result == 1) {
        this.openSnackBar('Recibo actualizado', 'Éxito');
        this.getReceipt();
      } else if (result == 2) {
        this.openSnackBar('Error al actualizar el recibo', 'Error');
      }
    });
  }

  openReceiptCancelledDialog() {
    const dialogRef = this.dialog.open(ReceiptCancelledComponent, {
      width: '900px'
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result == 1) {
        this.openSnackBar('Folio cancelado registrado', 'Éxito');
        this.getReceipt();
      } else if (result == 2) {
        this.openSnackBar('Error al registrar el folio cancelado', 'Error');
      }
    });
  }

  openSnackBar(message: string, action: string): MatSnackBarRef<SimpleSnackBar> {
    return this.snackBar.open(message, action, { duration: 3000 });
  }

}
