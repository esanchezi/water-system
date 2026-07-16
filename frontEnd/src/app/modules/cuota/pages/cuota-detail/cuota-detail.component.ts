import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { FeeService } from 'src/app/modules/shared/services/fee.service';
import { FeeAmountModel, FeeModel } from 'src/app/modules/shared/models/Fee.model';
import { CuotaAmountFormComponent } from '../../components/cuota-amount-form/cuota-amount-form.component';

@Component({
  selector: 'app-cuota-detail',
  templateUrl: './cuota-detail.component.html',
  styleUrls: ['./cuota-detail.component.css']
})
export class CuotaDetailComponent implements OnInit {

  private readonly route       = inject(ActivatedRoute);
  private readonly dialog      = inject(MatDialog);
  private readonly feeService  = inject(FeeService);

  fee!: FeeModel;

  displayColumns: string[] = ['vigencia', 'cuota', 'observaciones', 'actions'];
  dataSource = new MatTableDataSource<FeeAmountModel>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    const raw = this.route.snapshot.queryParams['element'];
    if (raw) {
      this.fee = JSON.parse(raw);
      this.loadAmounts();
    }
  }

  // Vuelve a pedir el listado completo de cuotas para refrescar esta
  // categoría con los montos más recientes (no hay endpoint de "una sola cuota").
  loadAmounts(): void {
    this.feeService.getFeeAmount().subscribe({
      next: (resp: any) => {
        if (resp.metadata?.code === '00') {
          const actualizado = (resp.data as FeeModel[]).find(f => f.cuotaId === this.fee.cuotaId);
          if (actualizado) {
            this.fee = actualizado;
          }
          const montos = [...(this.fee.amount ?? [])].sort((a, b) => b.vigencia - a.vigencia);
          this.dataSource = new MatTableDataSource<FeeAmountModel>(montos);
          this.dataSource.paginator = this.paginator;
        }
      },
      error: (e: any) => console.error(e)
    });
  }

  openAddAmount(): void {
    this.dialog.open(CuotaAmountFormComponent, {
      width: '450px',
      data: { cuotaId: this.fee.cuotaId, amount: null }
    }).afterClosed().subscribe(result => { if (result) this.loadAmounts(); });
  }

  openEditAmount(amount: FeeAmountModel): void {
    this.dialog.open(CuotaAmountFormComponent, {
      width: '450px',
      data: { cuotaId: this.fee.cuotaId, amount }
    }).afterClosed().subscribe(result => { if (result) this.loadAmounts(); });
  }
}
