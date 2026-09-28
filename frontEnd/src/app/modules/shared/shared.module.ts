import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MaterialModule } from '../core/material.module';
import { UserReceiptComponent } from './components/user-receipt/user-receipt.component';
import { UserCensoComponent } from './components/user-censo/user-censo.component';
import { UserUsoComponent } from './components/user-uso/user-uso.component';
import { UserChargesComponent } from './components/user-charges/user-charges.component';
import { AvisoAdeudoAtencionDialogComponent } from './components/aviso-adeudo-atencion-dialog/aviso-adeudo-atencion-dialog.component';
import { UserRevisionComponent } from './components/user-revision/user-revision.component';

@NgModule({
  declarations: [
    UserReceiptComponent,
    UserCensoComponent,
    UserUsoComponent,
    UserChargesComponent,
    AvisoAdeudoAtencionDialogComponent,
    UserRevisionComponent
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule
  ],
  exports: [
    UserReceiptComponent,
    UserCensoComponent,
    UserUsoComponent,
    UserChargesComponent,
    AvisoAdeudoAtencionDialogComponent,
    UserRevisionComponent
  ]
})
export class SharedModule { }
