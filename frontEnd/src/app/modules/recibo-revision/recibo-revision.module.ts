import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';
import { ReciboRevisionListComponent } from './pages/recibo-revision-list/recibo-revision-list.component';

@NgModule({
  declarations: [
    ReciboRevisionListComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    MaterialModule
  ],
  exports: [
    ReciboRevisionListComponent
  ]
})
export class ReciboRevisionModule { }
