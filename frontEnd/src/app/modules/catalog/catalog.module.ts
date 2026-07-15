import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../core/material.module';

import { CatalogListComponent } from './pages/catalog-list/catalog-list.component';
import { CatalogDetailComponent } from './pages/catalog-detail/catalog-detail.component';
import { CatalogFormComponent } from './components/catalog-form/catalog-form.component';
import { CatalogOptionFormComponent } from './components/catalog-option-form/catalog-option-form.component';

@NgModule({
  declarations: [
    CatalogListComponent,
    CatalogDetailComponent,
    CatalogFormComponent,
    CatalogOptionFormComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
  ]
})
export class CatalogModule { }
