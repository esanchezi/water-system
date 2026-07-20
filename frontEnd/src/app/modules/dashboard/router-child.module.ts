import { NgModule } from '@angular/core';

import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home.component';
import { UserComponent } from '../user/components/user/user.component';
import { ReceiptComponent } from '../receipt/components/receipt/receipt.component';
import { ReceiptHistoryComponent } from '../receipt-history/components/receipt-history/receipt-history.component';
import { DetailsUserComponent } from '../user/components/details-user/details-user.component';
import { WaterValvesComponent } from '../water-valves/components/water-valves/water-valves.component';
import { PeopleListComponent } from '../delegacion/people/people-list/people-list.component';
import { PeopleDetailsComponent } from '../delegacion/people/people-details/people-details.component';
import { AssemblyListComponent } from '../assembly/components/assembly-list/assembly-list.component';
import { AssemblyDetailsComponent } from '../assembly/components/assembly-details/assembly-details.component';
import { HouseListComponent } from '../house/pages/house-list/house-list.component';
import { HouseDetailsComponent } from '../house/pages/house-details/house-details.component';
import { PersonListComponent } from '../person/pages/person-list/person-list.component';
import { PersonDetailsComponent } from '../person/pages/person-details/person-details.component';
import { GroupListComponent } from '../group/pages/group-list/group-list.component';
import { GroupDetailsComponent } from '../group/pages/group-details/group-details.component';
import { CatalogListComponent } from '../catalog/pages/catalog-list/catalog-list.component';
import { CatalogDetailComponent } from '../catalog/pages/catalog-detail/catalog-detail.component';
import { ConvenioListComponent } from '../convenio/pages/convenio-list/convenio-list.component';
import { CuotaListComponent } from '../cuota/pages/cuota-list/cuota-list.component';
import { CuotaDetailComponent } from '../cuota/pages/cuota-detail/cuota-detail.component';
import { DeudorListComponent } from '../deudor/pages/deudor-list/deudor-list.component';
import { TotalPorAnioListComponent } from '../total-por-anio/pages/total-por-anio-list/total-por-anio-list.component';

export const childRoutes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'home', component: HomeComponent },
    { path: 'personList', component: PersonListComponent},
    { path: 'personaDetails', component: PersonDetailsComponent},
    { path: 'user', component: UserComponent },
    { path: 'receipt', component: ReceiptComponent },
    { path: 'receiptHistory', component: ReceiptHistoryComponent },
    { path: 'detailsUser', component: DetailsUserComponent },
    { path: 'waterValves', component: WaterValvesComponent },
    { path: 'peopleList', component: PeopleListComponent },
    { path: 'personDetails', component: PeopleDetailsComponent },
    { path: 'assemblyList', component: AssemblyListComponent },
    { path: 'assemblyDetails', component: AssemblyDetailsComponent },
    { path: 'houseList', component: HouseListComponent },
    { path: 'houseDetails', component: HouseDetailsComponent },
    { path: 'groupList', component: GroupListComponent},
    { path: 'groupDetails', component: GroupDetailsComponent},
    { path: 'catalogList', component: CatalogListComponent },
    { path: 'catalogDetail', component: CatalogDetailComponent },
    { path: 'convenioList', component: ConvenioListComponent },
    { path: 'cuotaList', component: CuotaListComponent },
    { path: 'cuotaDetail', component: CuotaDetailComponent },
    { path: 'deudorList', component: DeudorListComponent },
    { path: 'totalPorAnio', component: TotalPorAnioListComponent },
];

@NgModule({
    imports: [RouterModule.forChild(childRoutes)],
    exports: [RouterModule],
})
export class RouterChildModule { }
