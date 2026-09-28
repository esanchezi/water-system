import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AvisoPadronEntregaModel } from '../models/AvisoPadron.model';

@Injectable({ providedIn: 'root' })
export class AvisoPadronService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/avisoPadron`;

  getCandidatosPorCalle(calleId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/candidatos/porCalle/${calleId}`);
  }

  getHistorial(): Observable<any> {
    return this.http.get(`${this.baseUrl}/historial`);
  }

  // El PDF viene en el cuerpo; el motivo de un error va en el header
  // X-Error-Message -- mismo patrón que AvisoAdeudoService.generar()/
  // AvisoBombaService.generar().
  generar(aguaUsuarioIds: number[], fechaPresentacion: string): Observable<HttpResponse<Blob>> {
    return this.http.post(`${this.baseUrl}/generar`, { aguaUsuarioIds, fechaPresentacion }, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  marcarEntregada(avisoPadronId: number, datos: AvisoPadronEntregaModel): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoPadronId}/entregar`, datos);
  }

  cancelar(avisoPadronId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoPadronId}/cancelar`, {});
  }

  reactivar(avisoPadronId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoPadronId}/reactivar`, {});
  }
}
