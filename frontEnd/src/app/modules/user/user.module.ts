import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserComponent } from './components/user/user.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NewUserComponent } from './components/new-user/new-user.component';
import { DetailsUserComponent } from './components/details-user/details-user.component';
import { RenunciaTemporalDialogComponent } from './components/renuncia-temporal-dialog/renuncia-temporal-dialog.component';
import { RenunciaTemporalReconexionDialogComponent } from './components/renuncia-temporal-reconexion-dialog/renuncia-temporal-reconexion-dialog.component';
import { MaterialModule } from '../core/material.module';
import { SharedModule } from '../shared/shared.module';
import { ConvenioModule } from '../convenio/convenio.module';
import { GoogleMapsModule } from '@angular/google-maps';

@NgModule({
  declarations: [
    UserComponent,
    NewUserComponent,
    DetailsUserComponent,
    RenunciaTemporalDialogComponent,
    RenunciaTemporalReconexionDialogComponent,
  ],
  imports: [
    CommonModule,
    SharedModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    ConvenioModule,
    GoogleMapsModule,
  ]
})
export class UserModule { }
