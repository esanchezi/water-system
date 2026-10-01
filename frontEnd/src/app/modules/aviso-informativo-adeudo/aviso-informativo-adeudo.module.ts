import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';
import { SharedModule } from '../shared/shared.module';
import { AvisoInformativoAdeudoListComponent } from './pages/aviso-informativo-adeudo-list/aviso-informativo-adeudo-list.component';
import { AvisoInformativoAdeudoEntregaDialogComponent } from './components/aviso-informativo-adeudo-entrega-dialog/aviso-informativo-adeudo-entrega-dialog.component';

@NgModule({
  declarations: [
    AvisoInformativoAdeudoListComponent,
    AvisoInformativoAdeudoEntregaDialogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule,
    SharedModule
  ],
  exports: [
    AvisoInformativoAdeudoListComponent
  ]
})
export class AvisoInformativoAdeudoModule { }
