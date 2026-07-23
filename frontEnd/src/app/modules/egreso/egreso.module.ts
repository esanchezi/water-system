import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EgresoListComponent } from './pages/egreso-list/egreso-list.component';
import { EgresoGastosComponent } from './pages/egreso-gastos/egreso-gastos.component';
import { EgresoValesComponent } from './pages/egreso-vales/egreso-vales.component';
import { NewEgresoComponent } from './components/new-egreso/new-egreso.component';
import { NewGastoComponent } from './components/new-gasto/new-gasto.component';
import { EmitirValeComponent } from './components/emitir-vale/emitir-vale.component';
import { FusionarValesComponent } from './components/fusionar-vales/fusionar-vales.component';
import { EgresoNodoListaComponent } from './components/egreso-nodo-lista/egreso-nodo-lista.component';
import { ProveedorFormComponent } from './components/proveedor-form/proveedor-form.component';
import { MaterialModule } from '../core/material.module';

@NgModule({
  declarations: [
    EgresoListComponent,
    EgresoGastosComponent,
    EgresoValesComponent,
    NewEgresoComponent,
    NewGastoComponent,
    EmitirValeComponent,
    FusionarValesComponent,
    EgresoNodoListaComponent,
    ProveedorFormComponent
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  exports: [
    NewEgresoComponent,
    NewGastoComponent,
    EmitirValeComponent,
    FusionarValesComponent,
    ProveedorFormComponent
  ]
})
export class EgresoModule { }
