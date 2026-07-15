import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ConvenioListComponent } from './pages/convenio-list/convenio-list.component';
import { NewConvenioComponent } from './components/new-convenio/new-convenio.component';
import { MaterialModule } from '../core/material.module';

@NgModule({
  declarations: [
    ConvenioListComponent,
    NewConvenioComponent
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  exports: [
    NewConvenioComponent
  ]
})
export class ConvenioModule { }
