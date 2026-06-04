import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PersonListComponent } from './pages/person-list/person-list.component';
import { PersonDetailsComponent } from './pages/person-details/person-details.component';
import { MaterialModule } from '../core/material.module';
import { PersonFormComponent } from './components/person-form/person-form.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    PersonListComponent,
    PersonDetailsComponent,
    PersonFormComponent
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    SharedModule,
    ReactiveFormsModule,
  ]
})
export class PersonModule { }
