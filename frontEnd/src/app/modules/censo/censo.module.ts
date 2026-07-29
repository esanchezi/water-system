import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from '../core/material.module';

import { CensoResumenComponent } from './pages/censo-resumen/censo-resumen.component';

@NgModule({
  declarations: [
    CensoResumenComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
  ]
})
export class CensoModule { }
