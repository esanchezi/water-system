import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';

import { ResumenAnualListComponent } from './pages/resumen-anual-list/resumen-anual-list.component';

@NgModule({
  declarations: [
    ResumenAnualListComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
  ]
})
export class ResumenAnualModule { }
