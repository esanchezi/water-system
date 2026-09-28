import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { ValorGeneralService } from 'src/app/modules/shared/services/valor-general.service';
import { ValorGeneralModel } from 'src/app/modules/shared/models/ValorGeneral.model';
import { ValorGeneralFormDialogComponent } from '../../components/valor-general-form-dialog/valor-general-form-dialog.component';
import Swal from 'sweetalert2';

// Administración de la tabla de valores generales (multa por falta de
// pago, corte/reconexión, aviso, interés moratorio por día, multa de
// válvulas) -- un renglón por año (vigencia) por concepto, mismo espíritu
// que la pantalla de Cuotas pero sin la relación uso/tipo de usuario.
@Component({
  selector: 'app-valor-general-list',
  templateUrl: './valor-general-list.component.html',
  styleUrls: ['./valor-general-list.component.css']
})
export class ValorGeneralListComponent implements OnInit {

  private readonly valorGeneralService = inject(ValorGeneralService);
  private readonly dialog = inject(MatDialog);

  displayColumns: string[] = ['nombre', 'vigencia', 'monto', 'observaciones', 'actions'];
  dataSource = new MatTableDataSource<ValorGeneralModel>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.valorGeneralService.getAll().subscribe({
      next: (resp: any) => {
        if (resp.metadata?.[0]?.code === '00') {
          const datos: ValorGeneralModel[] = resp.data || [];
          // Se ordena por concepto y luego por año descendente, para ver
          // primero el valor más reciente de cada uno.
          datos.sort((a, b) => a.nombre.localeCompare(b.nombre) || b.vigencia - a.vigencia);
          this.dataSource = new MatTableDataSource<ValorGeneralModel>(datos);
          this.dataSource.paginator = this.paginator;
        }
      },
      error: (e: any) => console.error(e)
    });
  }

  openCreate(): void {
    this.dialog.open(ValorGeneralFormDialogComponent, { width: '480px', data: null })
      .afterClosed().subscribe(result => { if (result) this.load(); });
  }

  openEdit(valor: ValorGeneralModel): void {
    this.dialog.open(ValorGeneralFormDialogComponent, { width: '480px', data: valor })
      .afterClosed().subscribe(result => { if (result) this.load(); });
  }

  deactivate(valor: ValorGeneralModel): void {
    Swal.fire({
      icon: 'warning',
      title: 'Dar de baja',
      text: `¿Confirmas dar de baja "${valor.nombre}" del año ${valor.vigencia}?`,
      showCancelButton: true,
      confirmButtonText: 'Dar de baja',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (!result.isConfirmed) return;
      this.valorGeneralService.deactivate(valor.valorGeneralId).subscribe({
        next: () => this.load(),
        error: (e: any) => {
          console.error(e);
          Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo dar de baja el valor.', confirmButtonText: 'Cerrar' });
        }
      });
    });
  }
}
