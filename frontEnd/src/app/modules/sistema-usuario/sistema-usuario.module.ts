import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';
import { SistemaUsuarioListComponent } from './pages/sistema-usuario-list/sistema-usuario-list.component';

@NgModule({
  declarations: [
    SistemaUsuarioListComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    MaterialModule
  ],
  exports: [
    SistemaUsuarioListComponent
  ]
})
export class SistemaUsuarioModule { }
