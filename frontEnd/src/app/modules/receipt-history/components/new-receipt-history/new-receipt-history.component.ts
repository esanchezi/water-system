import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ReceiptHistoryService } from 'src/app/modules/shared/services/receipt-history.service';

@Component({
  selector: 'app-new-receipt-history',
  templateUrl: './new-receipt-history.component.html',
  styleUrls: ['./new-receipt-history.component.css']
})
export class NewReceiptHistoryComponent implements OnInit{

  private fb = inject(FormBuilder);
  public data = inject(MAT_DIALOG_DATA);
  private dialogRef= inject(MatDialogRef);
  private receiptService = inject(ReceiptHistoryService);
  public receiptForm!: FormGroup;

  estadoFormulario: string = "";
  
  ngOnInit(): void {
    if (this.data != null ){
      this.updateForm(this.data);
      this.estadoFormulario = "Actualizar";
    }
  }

  updateForm(data: any){
    this.receiptForm = this.fb.group( {
      noFolio: [data.element.noFolio],
      fecha: [data.element.fecha],
      concepto: [data.element.concepto],
      monto: [data.element.monto],
      nombre: [data.element.nombre],
      noUsuario: [],
      anio:[],
      fechaStr:[data.element.fecha],
    })
  }

  onSave(){
    let receipt = {
      aguaReciboHistorialId: this.data.element.aguaReciboHistorialId,
      anio: this.receiptForm.get('anio')?.value,
      fechaStr: this.receiptForm.get('fechaStr')?.value,
      waterUser: {
        noUsuario:this.receiptForm.get('noUsuario')?.value
      },
    }
    this.receiptService.saveReceiptHistory(receipt)
    .subscribe({
      next: () => this.dialogRef.close(1),
      error: () => this.dialogRef.close(2),
      complete: () => console.info('complete')
    });
  }
  onCancel(){}

}
