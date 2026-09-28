import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';
import { SharedModule } from '../shared/shared.module';
import { AvisoBombaListComponent } from './pages/aviso-bomba-list/aviso-bomba-list.component';
import { AvisoBombaEntregaDialogComponent } from './components/aviso-bomba-entrega-dialog/aviso-bomba-entrega-dialog.component';

@NgModule({
  declarations: [
    AvisoBombaListComponent,
    AvisoBombaEntregaDialogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    MaterialModule,
    SharedModule
  ],
  exports: [
    AvisoBombaListComponent
  ]
})
export class AvisoBombaModule { }
