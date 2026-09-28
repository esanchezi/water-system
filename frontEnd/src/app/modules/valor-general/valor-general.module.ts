import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';
import { ValorGeneralListComponent } from './pages/valor-general-list/valor-general-list.component';
import { ValorGeneralFormDialogComponent } from './components/valor-general-form-dialog/valor-general-form-dialog.component';

@NgModule({
  declarations: [
    ValorGeneralListComponent,
    ValorGeneralFormDialogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule
  ],
  exports: [
    ValorGeneralListComponent
  ]
})
export class ValorGeneralModule { }
