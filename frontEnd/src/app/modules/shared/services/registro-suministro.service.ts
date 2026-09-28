import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { RegistroSuministroModel } from '../models/WaterValves.model';

@Injectable({ providedIn: 'root' })
export class RegistroSuministroService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/registroSuministro`;

  getListRegistroSuministro(): Observable<any> {
    return this.http.get(`${this.baseUrl}/`);
  }

  getRegistroSuministroById(registroId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${registroId}`);
  }

  addRegistroSuministro(registro: Partial<RegistroSuministroModel>): Observable<any> {
    return this.http.post(`${this.baseUrl}/`, registro);
  }

  deleteRegistroSuministro(registroId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${registroId}`);
  }

  getDiasPorPozo(): Observable<any> {
    return this.http.get(`${this.baseUrl}/resumen/porPozo`);
  }

  getDiasPorTramo(): Observable<any> {
    return this.http.get(`${this.baseUrl}/resumen/porTramo`);
  }
}
