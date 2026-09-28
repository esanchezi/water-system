import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';
import { SharedModule } from '../shared/shared.module';
import { AvisoPadronListComponent } from './pages/aviso-padron-list/aviso-padron-list.component';
import { AvisoPadronEntregaDialogComponent } from './components/aviso-padron-entrega-dialog/aviso-padron-entrega-dialog.component';

@NgModule({
  declarations: [
    AvisoPadronListComponent,
    AvisoPadronEntregaDialogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    MaterialModule,
    SharedModule
  ],
  exports: [
    AvisoPadronListComponent
  ]
})
export class AvisoPadronModule { }
