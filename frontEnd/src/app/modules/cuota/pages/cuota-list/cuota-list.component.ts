import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { FeeService } from 'src/app/modules/shared/services/fee.service';
import { FeeAmountModel, FeeModel } from 'src/app/modules/shared/models/Fee.model';
import { CuotaFormComponent } from '../../components/cuota-form/cuota-form.component';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-cuota-list',
  templateUrl: './cuota-list.component.html',
  styleUrls: ['./cuota-list.component.css']
})
export class CuotaListComponent implements OnInit {

  private readonly feeService = inject(FeeService);
  private readonly dialog     = inject(MatDialog);
  private readonly router     = inject(Router);

  displayColumns: string[] = ['uso', 'userType', 'totalMontos', 'ultimoMonto', 'observaciones', 'actions'];
  dataSource = new MatTableDataSource<FeeModel>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.feeService.getFeeAmount().subscribe({
      next: (resp: any) => {
        if (resp.metadata?.[0]?.code === '00') {
          this.dataSource = new MatTableDataSource<FeeModel>(resp.data);
          this.dataSource.paginator = this.paginator;
        }
      },
      error: (e: any) => console.error(e)
    });
  }

  // El monto vigente "más reciente" es el de mayor año (vigencia) registrado.
  ultimoMonto(fee: FeeModel): FeeAmountModel | null {
    if (!fee.amount?.length) return null;
    return [...fee.amount].sort((a, b) => b.vigencia - a.vigencia)[0];
  }

  openCreate(): void {
    this.dialog.open(CuotaFormComponent, { width: '500px', data: null })
      .afterClosed().subscribe(result => { if (result) this.load(); });
  }

  openEdit(fee: FeeModel): void {
    this.dialog.open(CuotaFormComponent, { width: '500px', data: fee })
      .afterClosed().subscribe(result => { if (result) this.load(); });
  }

  goToDetail(fee: FeeModel): void {
    this.router.navigate(['dashboard/cuotaDetail'], {
      queryParams: { element: JSON.stringify(fee) }
    });
  }

  // Baja lógica: pone estatus = 0, no borra el registro.
  deactivate(fee: FeeModel): void {
    Swal.fire({
      icon: 'warning',
      title: 'Dar de baja cuota',
      text: `¿Confirmas dar de baja "${fee.userType?.nombre ?? ''} - ${fee.uso?.nombre ?? ''}"? Dejará de estar disponible para asignar a usuarios.`,
      showCancelButton: true,
      confirmButtonText: 'Dar de baja',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (!result.isConfirmed) return;
      this.feeService.deactivate(fee.cuotaId).subscribe({
        next: () => this.load(),
        error: (e: any) => {
          console.error(e);
          Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo dar de baja la cuota.', confirmButtonText: 'Cerrar' });
        }
      });
    });
  }
}
