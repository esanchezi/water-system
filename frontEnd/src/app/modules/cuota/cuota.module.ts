import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';

import { CuotaListComponent } from './pages/cuota-list/cuota-list.component';
import { CuotaDetailComponent } from './pages/cuota-detail/cuota-detail.component';
import { CuotaFormComponent } from './components/cuota-form/cuota-form.component';
import { CuotaAmountFormComponent } from './components/cuota-amount-form/cuota-amount-form.component';

@NgModule({
  declarations: [
    CuotaListComponent,
    CuotaDetailComponent,
    CuotaFormComponent,
    CuotaAmountFormComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
  ]
})
export class CuotaModule { }
