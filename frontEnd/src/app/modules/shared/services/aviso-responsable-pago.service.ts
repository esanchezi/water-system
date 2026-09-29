import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AvisoResponsablePagoEntregaModel, AvisoResponsablePagoPersonaModel } from '../models/AvisoResponsablePago.model';

@Injectable({ providedIn: 'root' })
export class AvisoResponsablePagoService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/avisoResponsablePago`;

  getUsuariosDeLaCasa(casaId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/casa/${casaId}/usuarios`);
  }

  getHistorial(): Observable<any> {
    return this.http.get(`${this.baseUrl}/historial`);
  }

  getHistorialPorCasa(casaId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/casa/${casaId}/historial`);
  }

  // El PDF viene en el cuerpo; el motivo de un error va en el header
  // X-Error-Message -- mismo patrón que las demás cartas.
  generar(datos: {
    casaId: number;
    motivoSolicitud?: string;
    fechaSolicitud?: string;
    observacionesComite?: string;
    personas: AvisoResponsablePagoPersonaModel[];
  }): Observable<HttpResponse<Blob>> {
    return this.http.post(`${this.baseUrl}/generar`, datos, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  marcarEntregada(responsablePagoId: number, datos: AvisoResponsablePagoEntregaModel): Observable<any> {
    return this.http.put(`${this.baseUrl}/${responsablePagoId}/entregar`, datos);
  }

  cancelar(responsablePagoId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${responsablePagoId}/cancelar`, {});
  }

  reactivar(responsablePagoId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${responsablePagoId}/reactivar`, {});
  }
}
