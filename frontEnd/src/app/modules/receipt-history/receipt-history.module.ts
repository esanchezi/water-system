import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReceiptHistoryComponent } from './components/receipt-history/receipt-history.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NewReceiptHistoryComponent } from './components/new-receipt-history/new-receipt-history.component';
import { MaterialModule } from '../core/material.module';

@NgModule({
  declarations: [
    ReceiptHistoryComponent,
    NewReceiptHistoryComponent
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class ReceiptHistoryModule { }
