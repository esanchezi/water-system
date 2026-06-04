import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from '../core/material.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { GroupListComponent } from './pages/group-list/group-list.component';
import { GroupDetailsComponent } from './pages/group-details/group-details.component';

@NgModule({
  declarations: [ GroupListComponent, GroupDetailsComponent],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    SharedModule,
    ReactiveFormsModule
  ]
  
})
export class GroupModule { }
