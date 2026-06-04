import { NgModule,CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WaterValvesComponent } from './components/water-valves/water-valves.component';
import { GoogleMapsModule } from '@angular/google-maps';

@NgModule({
  declarations: [
    WaterValvesComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    GoogleMapsModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  exports: [
    WaterValvesComponent
  ]
})
export class WaterValvesModule { }
