import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardComponent } from './pages/dashboard.component';
import { HomeComponent } from './components/home.component';
import { SharedModule } from '../shared/shared.module';
import { UserModule } from '../user/user.module';
import { ReceiptModule } from '../receipt/receipt.module';
import { ReceiptHistoryModule } from '../receipt-history/receipt-history.module';
import { PeopleModuleModule } from '../delegacion/people/people-module.module';
import { AssemblyModule } from '../assembly/assembly-module.module';
import { HouseModule } from '../house/house-module.module';
import { MaterialModule } from '../core/material.module';
import { CoreModule } from '../core/core.module';
import { PersonModule } from '../person/person-module.module';
import { GroupModule } from '../group/group-module.module';
import { CatalogModule } from '../catalog/catalog.module';
import { ConvenioModule } from '../convenio/convenio.module';
import { CuotaModule } from '../cuota/cuota.module';
import { DeudorModule } from '../deudor/deudor.module';
import { TotalPorAnioModule } from '../total-por-anio/total-por-anio.module';
import { EgresoModule } from '../egreso/egreso.module';
import { ResumenAnualModule } from '../resumen-anual/resumen-anual.module';

@NgModule({
  declarations: [
    DashboardComponent,
    HomeComponent,
  ],
  imports: [
    CommonModule,
    SharedModule,
    UserModule,
    ReceiptModule,
    ReceiptHistoryModule,
    PeopleModuleModule,
    AssemblyModule,
    HouseModule,
    MaterialModule,
    CoreModule,
    PersonModule,
    GroupModule,
    CatalogModule,
    ConvenioModule,
    CuotaModule,
    DeudorModule,
    TotalPorAnioModule,
    EgresoModule,
    ResumenAnualModule
  ]
})
export class DashboardModule { }
