import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MaterialModule } from '../core/material.module';
import { UserReceiptComponent } from './components/user-receipt/user-receipt.component';

@NgModule({
  declarations: [UserReceiptComponent],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule
  ],
  exports: [UserReceiptComponent]
})
export class SharedModule { }
