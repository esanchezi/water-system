import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';
import { SharedModule } from '../shared/shared.module';
import { AvisoAdeudoListComponent } from './pages/aviso-adeudo-list/aviso-adeudo-list.component';
import { AvisoAdeudoEntregaDialogComponent } from './components/aviso-adeudo-entrega-dialog/aviso-adeudo-entrega-dialog.component';

@NgModule({
  declarations: [
    AvisoAdeudoListComponent,
    AvisoAdeudoEntregaDialogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    SharedModule
  ],
  exports: [
    AvisoAdeudoListComponent
  ]
})
export class AvisoAdeudoModule { }
