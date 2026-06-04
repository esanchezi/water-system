import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AssemblyListComponent } from './components/assembly-list/assembly-list.component';
import { AssemblyDetailsComponent } from './components/assembly-details/assembly-details.component';
import { AssemblyNewAttendanceComponent } from './components/assembly-new-attendance/assembly-new-attendance.component';
import { MaterialModule } from '../core/material.module';

@NgModule({
  declarations: [
    AssemblyListComponent,
    AssemblyDetailsComponent,
    AssemblyNewAttendanceComponent
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    SharedModule,
    ReactiveFormsModule
  ]
})
export class AssemblyModule{ }
