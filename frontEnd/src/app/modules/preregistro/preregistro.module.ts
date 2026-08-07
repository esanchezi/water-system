import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';

import { PreregistroListComponent } from './pages/preregistro-list/preregistro-list.component';

@NgModule({
  declarations: [
    PreregistroListComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
  ]
})
export class PreregistroModule { }
