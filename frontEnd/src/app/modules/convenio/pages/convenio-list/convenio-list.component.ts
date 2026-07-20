import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import { WaterAgreementModel } from 'src/app/modules/shared/models/WaterAgreement.model';
import { AgreementService } from 'src/app/modules/shared/services/agreement.service';
import { NewConvenioComponent } from '../../components/new-convenio/new-convenio.component';

@Component({
  selector: 'app-convenio-list',
  templateUrl: './convenio-list.component.html',
  styleUrls: ['./convenio-list.component.css']
})
export class ConvenioListComponent implements OnInit {

  private readonly agreementService = inject(AgreementService);
  private readonly snackBar = inject(MatSnackBar);
  public dialog = inject(MatDialog);

  displayColumns: string[] = ['noFolio', 'fechaStr', 'noUsuario', 'nombreUsuario', 'motivo', 'adeudo', 'fechaCompromisoPagoStr', 'montoCondonadoTotal', 'estatusConvenio'];
  dataSource = new MatTableDataSource<WaterAgreementModel>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.getAgreements();
  }

  getAgreements(): void {
    this.agreementService.getAll().subscribe({
      next: (resp: any) => this.processResponse(resp),
      error: (e: any) => console.error(e)
    });
  }

  private processResponse(resp: any): void {
    if (resp.metadata[0].code !== '00') return;
    const data: WaterAgreementModel[] = resp.data;
    this.dataSource = new MatTableDataSource<WaterAgreementModel>(data);
    this.dataSource.paginator = this.paginator;
  }

  openNewConvenioDialog(): void {
    const dialogRef = this.dialog.open(NewConvenioComponent, {
      width: '900px'
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result === 1) {
        this.openSnackBar('Convenio registrado', 'Éxito');
        this.getAgreements();
      } else if (result === 2) {
        this.openSnackBar('Error al guardar el convenio', 'Error');
      }
    });
  }

  openSnackBar(message: string, action: string): MatSnackBarRef<SimpleSnackBar> {
    return this.snackBar.open(message, action, { duration: 2000 });
  }
}
