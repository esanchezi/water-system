import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';

import { DeudorListComponent } from './pages/deudor-list/deudor-list.component';

@NgModule({
  declarations: [
    DeudorListComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
  ]
})
export class DeudorModule { }
