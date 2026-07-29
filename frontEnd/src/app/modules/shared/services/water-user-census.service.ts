import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { WaterUserCensusCreateModel } from '../models/WaterUserCensus.model';

const BASE = `${environment.apiUrl}/waterUserCensus`;

@Injectable({
  providedIn: 'root'
})
export class WaterUserCensusService {

  private readonly http = inject(HttpClient);

  getByAguaUsuarioId(aguaUsuarioId: number): Observable<any> {
    return this.http.get(`${BASE}/usuario/${aguaUsuarioId}`);
  }

  create(aguaUsuarioId: number, body: WaterUserCensusCreateModel): Observable<any> {
    return this.http.post(`${BASE}/${aguaUsuarioId}`, body);
  }

  update(censoId: number, body: WaterUserCensusCreateModel): Observable<any> {
    return this.http.put(`${BASE}/${censoId}`, body);
  }

  deactivate(censoId: number): Observable<any> {
    return this.http.put(`${BASE}/${censoId}/deactivate`, {});
  }

  getResumenPorEdad(): Observable<any> {
    return this.http.get(`${BASE}/resumen`);
  }
}
