import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HouseListComponent } from './pages/house-list/house-list.component';
import { HouseDetailsComponent } from './pages/house-details/house-details.component';
import { HouseNewComponent } from './pages/house-new/house-new.component';
import { GoogleMapsModule } from "@angular/google-maps";
import { HouseFormComponent } from './components/house-form/house-form.component';
import { MaterialModule } from '../core/material.module';


@NgModule({
  declarations: [
    HouseListComponent,
    HouseDetailsComponent,
    HouseNewComponent,
    HouseFormComponent
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    SharedModule,
    ReactiveFormsModule,
    GoogleMapsModule
]
})
export class HouseModule { }
