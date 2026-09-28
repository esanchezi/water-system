import {  NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { DashboardModule } from './modules/dashboard/dashboard.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgChartsModule } from 'ng2-charts';
import { GoogleMapsModule } from '@angular/google-maps';
import { WaterValvesModule } from './modules/water-valves/water-valves.module';
import { registerLocaleData } from '@angular/common';
import localeEsMx from '@angular/common/locales/es-MX';
import { CurrencyPipe } from '@angular/common';
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { LoginModule } from './modules/login/login.module';
import { AuthInterceptor } from './modules/shared/interceptors/auth.interceptor';
import { ConfiguracionModule } from './modules/configuracion/configuracion.module';
import { SistemaUsuarioModule } from './modules/sistema-usuario/sistema-usuario.module';
import { ReciboRevisionModule } from './modules/recibo-revision/recibo-revision.module';
import { AvisoAdeudoModule } from './modules/aviso-adeudo/aviso-adeudo.module';
import { AvisoBombaModule } from './modules/aviso-bomba/aviso-bomba.module';
import { AvisoPadronModule } from './modules/aviso-padron/aviso-padron.module';
import { ValorGeneralModule } from './modules/valor-general/valor-general.module';

registerLocaleData(localeEsMx);

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    DashboardModule,
    BrowserAnimationsModule,
    NgChartsModule,
    GoogleMapsModule,
    WaterValvesModule,
    MatCardModule,
    MatFormFieldModule,
    LoginModule,
    ConfiguracionModule,
    SistemaUsuarioModule,
    ReciboRevisionModule,
    AvisoAdeudoModule,
    AvisoBombaModule,
    AvisoPadronModule,
    ValorGeneralModule
],
  providers: [
    CurrencyPipe,
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
