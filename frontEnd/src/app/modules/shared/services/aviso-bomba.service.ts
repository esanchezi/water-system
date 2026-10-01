import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AvisoBombaEntregaModel } from '../models/AvisoBomba.model';

@Injectable({ providedIn: 'root' })
export class AvisoBombaService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/avisoBomba`;

  getCandidatosPorCalle(calleId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/candidatos/porCalle/${calleId}`);
  }

  getHistorial(): Observable<any> {
    return this.http.get(`${this.baseUrl}/historial`);
  }

  // Historial completo de un usuario específico -- para el acordeón
  // "Cartas generadas" en su ficha.
  getPorUsuario(aguaUsuarioId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/porUsuario/${aguaUsuarioId}`);
  }

  // El PDF viene en el cuerpo; el motivo de un error va en el header
  // X-Error-Message -- mismo patrón que AvisoAdeudoService.generar().
  generar(aguaUsuarioIds: number[], fechaReporte: string): Observable<HttpResponse<Blob>> {
    return this.http.post(`${this.baseUrl}/generar`, { aguaUsuarioIds, fechaReporte }, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  marcarEntregada(avisoBombaId: number, datos: AvisoBombaEntregaModel): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoBombaId}/entregar`, datos);
  }

  cancelar(avisoBombaId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoBombaId}/cancelar`, {});
  }

  reactivar(avisoBombaId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoBombaId}/reactivar`, {});
  }
}
