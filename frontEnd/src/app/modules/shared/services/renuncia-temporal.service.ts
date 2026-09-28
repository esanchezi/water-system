import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { RenunciaTemporalCrearModel, RenunciaTemporalReconexionModel } from '../models/RenunciaTemporal.model';

@Injectable({ providedIn: 'root' })
export class RenunciaTemporalService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/renunciaTemporal`;

  // Igual patrón que AvisoAdeudoService.generar(): el PDF viene en el
  // cuerpo de la respuesta, el error (si lo hay) va en el header
  // X-Error-Message porque el cuerpo ya está ocupado.
  generar(datos: RenunciaTemporalCrearModel): Observable<HttpResponse<Blob>> {
    return this.http.post(`${this.baseUrl}/generar`, datos, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  getHistorialPorUsuario(aguaUsuarioId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/historial/${aguaUsuarioId}`);
  }

  reconectar(renunciaTemporalId: number, datos: RenunciaTemporalReconexionModel): Observable<any> {
    return this.http.put(`${this.baseUrl}/${renunciaTemporalId}/reconectar`, datos);
  }

  cancelar(renunciaTemporalId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${renunciaTemporalId}/cancelar`, {});
  }

  activaPorUsuario(aguaUsuarioId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/activaPorUsuario/${aguaUsuarioId}`);
  }
}
