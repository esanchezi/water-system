import { Component, Input, ViewChild, inject } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReceiptService } from '../../services/receipt.service';
import { WaterReceiptModel } from '../../models/WaterReceipt.model';
import { WaterUserModel } from '../../models/WaterUser.model';
import { CatalogData, CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { NewReceiptComponent } from 'src/app/modules/receipt/components/new-receipt/new-receipt.component';

@Component({
  selector: 'app-user-receipt',
  templateUrl: './user-receipt.component.html',
  styleUrls: ['./user-receipt.component.css']
})
export class UserReceiptComponent {
  @Input() usuario!: WaterUserModel;

  filters = {
    concepto: '',
    anio: ''
  };

  displayColumns: string[] = [
    'noFolio', 'fecha', 'concepto', 'total','observaciones',
    'conceptoPayment', 'montoRecibido', 'montoAplicado', 'anio'
  ];

  dataSource = new MatTableDataSource<any>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  private readonly receiptService = inject(ReceiptService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  // Abre el mismo formulario de "Agregar recibo" que usa el módulo de
  // Pagos, pero con el usuario ya cargado (se conoce desde esta ficha) --
  // solo falta capturar el folio y el resto del recibo. Ver
  // NewReceiptComponent.usuarioBloqueado.
  agregarRecibo(): void {
    if (!this.usuario) {
      return;
    }
    const persona = this.usuario.person;
    const nombreCompleto = [persona?.nombre, persona?.nombre2, persona?.app, persona?.apm]
      .filter(Boolean).join(' ');
    const dialogRef = this.dialog.open(NewReceiptComponent, {
      width: '1200px',
      data: {
        usuarioPreseleccionado: {
          noUsuario: Number(this.usuario.noUsuario),
          nombreCompleto,
          aguaUsuarioId: this.usuario.aguaUsuarioId
        }
      }
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result === 1) {
        this.snackBar.open('Recibo agregado', 'Éxito', { duration: 3000 });
        this.getReceipt();
      } else if (result === 2) {
        this.snackBar.open('No se pudo guardar el recibo', 'Error', { duration: 3000 });
      }
    });
  }

  getReceipt() {
    if (!this.usuario) return;

    this.receiptService.getReceiptByNoUser(this.usuario.noUsuario)
      .subscribe({
        next: (resp: any) => this.processReceiptResponse(resp)
      });
  }

  private processReceiptResponse(resp: any) {
    const flatData: any[] = [];
    if (resp.metadata[0].code == "00") {
      let listUser = resp.data;

      listUser.forEach((recibo: WaterReceiptModel) => {
        recibo.waterReceiptPayment.forEach(pago => {
          flatData.push({
            ...pago,
            fechaPago: recibo.fecha,
            noFolio: recibo.noFolio,
            conceptoReceipt: recibo.concepto,
            observaciones: recibo.observaciones,
            total: recibo.total,
            waterUser: recibo.waterUser
          });
        });
      });

      this.dataSource = new MatTableDataSource<any>(flatData);
      this.dataSource.paginator = this.paginator;

      this.dataSource.filterPredicate = (data: any, filter: string) => {
        const searchTerms = JSON.parse(filter);
        const matchConcepto = !searchTerms.concepto || data.concepto?.toLowerCase().includes(searchTerms.concepto);
        const matchAnio = !searchTerms.anio || data.anio?.toString().includes(searchTerms.anio);

        return matchConcepto && matchAnio;
      };

      // Recalcular totales al cargar (sin filtro aplicado todavía)
      this.calcularTotales(flatData);
    }
  }

  applyFilters(): void {
    this.dataSource.filter = JSON.stringify(this.filters);
    // Los totales deben reflejar solo lo que está visible según los filtros activos
    this.calcularTotales(this.dataSource.filteredData);
  }

  // Total al pie de la tabla — se recalcula con cada filtro para ver
  // fácilmente lo aportado por el usuario según lo que esté visible.
  // Solo "Monto Aplicado": el total de "Monto Recibido" se quitó porque
  // puede confundir (un mismo recibo puede aplicarse a varios conceptos).
  totalMontoAplicado = 0;

  private calcularTotales(rows: any[]): void {
    this.totalMontoAplicado = rows.reduce((acc, r) => acc + (Number(r.montoAplicado) || 0), 0);
  }

  applyAnioFilter(event: Event): void {
    this.filters.anio = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.applyFilters();
  }

  applyConceptoFilter(event: Event): void {
    this.filters.concepto = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.applyFilters();
  }
}