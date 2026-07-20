import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';

import { TotalPorAnioListComponent } from './pages/total-por-anio-list/total-por-anio-list.component';

@NgModule({
  declarations: [
    TotalPorAnioListComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
  ]
})
export class TotalPorAnioModule { }
