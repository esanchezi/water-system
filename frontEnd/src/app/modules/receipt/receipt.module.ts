import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReceiptComponent } from './components/receipt/receipt.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NewReceiptComponent } from './components/new-receipt/new-receipt.component';
import { ReceiptCancelledComponent } from './components/receipt-cancelled/receipt-cancelled.component';
import { MaterialModule } from '../core/material.module';

@NgModule({
  declarations: [
    ReceiptComponent,
    NewReceiptComponent,
    ReceiptCancelledComponent
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
  ]
})
export class ReceiptModule { }
