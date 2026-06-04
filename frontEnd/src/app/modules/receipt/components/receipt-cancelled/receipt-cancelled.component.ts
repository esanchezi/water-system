import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReceiptService } from 'src/app/modules/shared/services/receipt.service';

@Component({
  selector: 'app-receipt-cancelled',
  templateUrl: './receipt-cancelled.component.html',
  styleUrls: ['./receipt-cancelled.component.css']
})
export class ReceiptCancelledComponent implements OnInit{

  private readonly receiptService = inject(ReceiptService);
  private readonly fb = inject(FormBuilder);

  public receiptForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
  }

   private initForm(): void {
      this.receiptForm = this.fb.group({
        noFolio: ['',Validators.required],
        fecha: ['',Validators.required],
      });
    }

  onSave(){
    let data ={
      noFolio: this.receiptForm.get('noFolio')?.value,
      fechaStr: this.receiptForm.get('fecha')?.value,
    }
    this.saveReceiptCancelledData(data);
  }

  onCancel(){}


  private saveReceiptCancelledData(data: any): void {
    this.receiptService.saveReceiptCancelled(data)
      .subscribe({
        next: () => console.info('complete'),//this.dialogRef.close(1),
        error: () => console.info('error'),//this.dialogRef.close(2),
        complete: () => console.info('complete')
      });
  }

}
