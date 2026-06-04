import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { WaterReceiptHistoryModel } from 'src/app/modules/shared/models/WaterReceiptHistory.model';
import { ReceiptHistoryService } from 'src/app/modules/shared/services/receipt-history.service';
import { NewReceiptHistoryComponent } from '../new-receipt-history/new-receipt-history.component';

@Component({
  selector: 'app-receipt-history',
  templateUrl: './receipt-history.component.html',
  styleUrls: ['./receipt-history.component.css']
})
export class ReceiptHistoryComponent implements OnInit{

  private receiptService = inject(ReceiptHistoryService);
  private snackBar = inject(MatSnackBar);
  displayColumns: string[]=['noFolio','fecha','concepto','total','usuario','nombre','direccion','comite','procesado','actions'];
  dataSource = new MatTableDataSource<WaterReceiptHistoryModel>();
  public dialog = inject(MatDialog);

  ngOnInit(): void {
    this.getReceipt();
  }

  @ViewChild(MatPaginator)
  paginator !: MatPaginator;

  buscar(name: any){
    if ( name.length === 0){
      return this.getReceipt();
    }

    this.receiptService.getReceiptByNoFolioOrNoUser(name)
        .subscribe({
          next: (v) => this.processReceiptResponse(v)
        })
  }

  getReceipt():void{
    this.receiptService.getReceipt()
      .subscribe({
        next: (v) => this.processReceiptResponse(v),
        error: (e) => console.error(e),
        complete: () => console.info('complete') 
      });
  }

  processReceiptResponse(resp:any){
    const dataUser: WaterReceiptHistoryModel[] = [];
    console.log(resp);
    if (resp.metadata[0].code == "00"){
      let listUser = resp.data;

      listUser.forEach((element:WaterReceiptHistoryModel)=> {
        dataUser.push(element);
      });
      this.dataSource = new MatTableDataSource<WaterReceiptHistoryModel>(dataUser);
      this.dataSource.paginator = this.paginator;
    }
  }

  edit(element:WaterReceiptHistoryModel){
    console.log("Editar");
    console.log(element);
    const dialogRef = this.dialog.open(NewReceiptHistoryComponent , {
      width: '900px', 
      data: {element:element}
    });
  }
 
  openSnackBar(message:string, action:string): MatSnackBarRef<SimpleSnackBar>{
    return this.snackBar.open(message,action,{duration:2000})
  }

}
