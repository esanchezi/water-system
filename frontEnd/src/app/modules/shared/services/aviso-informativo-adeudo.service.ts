import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AvisoInformativoAdeudoEntregaModel, UsuarioInformativoAdeudoModel } from '../models/AvisoInformativoAdeudo.model';

@Injectable({ providedIn: 'root' })
export class AvisoInformativoAdeudoService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/avisoInformativoAdeudo`;

  getHistorial(): Observable<any> {
    return this.http.get(`${this.baseUrl}/historial`);
  }

  // Historial completo de un usuario específico -- para el acordeón
  // "Cartas generadas" en su ficha.
  getPorUsuario(aguaUsuarioId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/porUsuario/${aguaUsuarioId}`);
  }

  // El PDF viene en el cuerpo; el motivo de un error va en el header
  // X-Error-Message -- mismo patrón que AvisoBombaService.generar().
  generar(usuarios: UsuarioInformativoAdeudoModel[], fechaPresentacion: string): Observable<HttpResponse<Blob>> {
    const payload = {
      usuarios: usuarios.map(u => ({
        aguaUsuarioId: u.aguaUsuarioId,
        observacion: u.observacion
      })),
      fechaPresentacion
    };
    return this.http.post(`${this.baseUrl}/generar`, payload, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  marcarEntregada(avisoInformativoAdeudoId: number, datos: AvisoInformativoAdeudoEntregaModel): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoInformativoAdeudoId}/entregar`, datos);
  }

  cancelar(avisoInformativoAdeudoId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoInformativoAdeudoId}/cancelar`, {});
  }

  reactivar(avisoInformativoAdeudoId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoInformativoAdeudoId}/reactivar`, {});
  }
}
