import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';
import { SharedModule } from '../shared/shared.module';
import { AvisoResponsablePagoListComponent } from './pages/aviso-responsable-pago-list/aviso-responsable-pago-list.component';
import { AvisoResponsablePagoEntregaDialogComponent } from './components/aviso-responsable-pago-entrega-dialog/aviso-responsable-pago-entrega-dialog.component';

@NgModule({
  declarations: [
    AvisoResponsablePagoListComponent,
    AvisoResponsablePagoEntregaDialogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    SharedModule
  ],
  exports: [
    AvisoResponsablePagoListComponent
  ]
})
export class AvisoResponsablePagoModule { }
