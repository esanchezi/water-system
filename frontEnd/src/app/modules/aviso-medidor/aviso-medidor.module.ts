import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';
import { SharedModule } from '../shared/shared.module';
import { AvisoMedidorListComponent } from './pages/aviso-medidor-list/aviso-medidor-list.component';
import { AvisoMedidorEntregaDialogComponent } from './components/aviso-medidor-entrega-dialog/aviso-medidor-entrega-dialog.component';

@NgModule({
  declarations: [
    AvisoMedidorListComponent,
    AvisoMedidorEntregaDialogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    SharedModule
  ],
  exports: [
    AvisoMedidorListComponent
  ]
})
export class AvisoMedidorModule { }
