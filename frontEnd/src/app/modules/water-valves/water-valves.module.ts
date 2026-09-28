import { NgModule,CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WaterValvesComponent } from './components/water-valves/water-valves.component';
import { GoogleMapsModule } from '@angular/google-maps';
import { MaterialModule } from '../core/material.module';

@NgModule({
  declarations: [
    WaterValvesComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    GoogleMapsModule,
    MaterialModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  exports: [
    WaterValvesComponent
  ]
})
export class WaterValvesModule { }
