import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { WaterReceiptModel } from 'src/app/modules/shared/models/WaterReceipt.model';
import { ReceiptService } from '../../../shared/services/receipt.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import { NewReceiptComponent } from '../new-receipt/new-receipt.component';
import { ReceiptCancelledComponent } from '../receipt-cancelled/receipt-cancelled.component';

@Component({
  selector: 'app-receipt',
  templateUrl: './receipt.component.html',
  styleUrls: ['./receipt.component.css']
})
export class ReceiptComponent implements OnInit{
  
  private receiptService = inject(ReceiptService);
  private snackBar = inject(MatSnackBar);
  displayColumns: string[]=['noFolio','concepto','observaciones','total','usuario','conceptoPayment','montoRecibido','montoAplicado','anio','actions'];
  dataSource = new MatTableDataSource<WaterReceiptModel>();
  public dialog = inject(MatDialog);

  @ViewChild(MatPaginator)
  paginator !: MatPaginator;

  ngOnInit(): void {
    this.getReceipt();
  }

  buscar(name: any){
    if ( name.length === 0){
      return this.getReceipt();
    }

    this.receiptService.getReceiptByNoFolioOrNoUser(name)
        .subscribe({
          next: (v) => this.processReceiptResponse(v)
        })
  }
 
  exportExcel(){}

  getReceipt():void{
    this.receiptService.getReceipt()
      .subscribe({
        next: (v) => this.processReceiptResponse(v),
        error: (e) => console.error(e),
        complete: () => console.info('complete') 
      });
  }

  processReceiptResponse(resp: any) {
    const flatData: any[] = [];
  
    if (resp.metadata[0].code === "00") {
      const listUser = resp.data;
  
      listUser.forEach((recibo: WaterReceiptModel) => {
        if (recibo.waterReceiptPayment && recibo.waterReceiptPayment.length > 0) {
          // Caso con pagos: lo divide por cada uno
          recibo.waterReceiptPayment.forEach(pago => {
            flatData.push({
              ...pago,
              noFolio: recibo.noFolio,
              conceptoReceipt: recibo.concepto,
              observaciones: recibo.observaciones,
              total: recibo.total,
              waterUser: recibo.waterUser
            });
          });
        } else {
          // Caso sin pagos: igual se muestra el recibo
          flatData.push({
            noFolio: recibo.noFolio,
            conceptoReceipt: recibo.concepto,
            observaciones: recibo.observaciones,
            total: recibo.total,
            waterUser: recibo.waterUser,
            concepto: null,
            montoAplicado: null,
            anio: null
          });
        }
      });
  
      this.dataSource = new MatTableDataSource<any>(flatData);
      this.dataSource.paginator = this.paginator;
    }
  }

  openReciboDialog(){
    const dialogRef = this.dialog.open(NewReceiptComponent,{
      width:'1200px'
    });

    dialogRef.afterClosed().subscribe((result:any) => {
      if(result == 1){
        this.openSnackBar("Categoria agregada","Exito")
        this.getReceipt();
      }else if (result == 2){
        this.openSnackBar("Error al guardar categoria","Error")
      }
    });
  }

  openReceiptCancelledDialog(){
    const dialogRef = this.dialog.open(ReceiptCancelledComponent,{
      width:'900px'
    });

    dialogRef.afterClosed().subscribe((result:any) => {
      if(result == 1){
        this.openSnackBar("Categoria agregada","Exito")
        this.getReceipt();
      }else if (result == 2){
        this.openSnackBar("Error al guardar categoria","Error")
      }
    });
  }

  openSnackBar(message:string, action:string): MatSnackBarRef<SimpleSnackBar>{
    return this.snackBar.open(message,action,{duration:2000})
  }

}
