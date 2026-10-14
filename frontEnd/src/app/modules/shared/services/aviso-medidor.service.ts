import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AvisoMedidorEntregaModel } from '../models/AvisoMedidor.model';

@Injectable({ providedIn: 'root' })
export class AvisoMedidorService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/avisoMedidor`;

  getHistorial(): Observable<any> {
    return this.http.get(`${this.baseUrl}/historial`);
  }

  // Historial completo de un usuario específico -- para el acordeón
  // "Cartas generadas" en su ficha.
  getPorUsuario(aguaUsuarioId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/porUsuario/${aguaUsuarioId}`);
  }

  // El PDF viene en el cuerpo; el motivo de un error va en el header
  // X-Error-Message -- mismo patrón que AvisoBombaService.generar(). A
  // diferencia de Bomba, aquí se genera de a uno, con sus propios datos de
  // instalación (número de serie y lectura inicial no tendría sentido
  // repetirlos para varios usuarios en una sola llamada).
  generar(aguaUsuarioId: number, fechaInstalacion: string, noSerieMedidor: string, lecturaInicialMedidor: string): Observable<HttpResponse<Blob>> {
    return this.http.post(`${this.baseUrl}/generar`,
      { aguaUsuarioId, fechaInstalacion, noSerieMedidor, lecturaInicialMedidor },
      { observe: 'response', responseType: 'blob' });
  }

  marcarEntregada(avisoMedidorId: number, datos: AvisoMedidorEntregaModel): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoMedidorId}/entregar`, datos);
  }

  cancelar(avisoMedidorId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoMedidorId}/cancelar`, {});
  }

  reactivar(avisoMedidorId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoMedidorId}/reactivar`, {});
  }
}
