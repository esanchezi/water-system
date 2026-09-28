import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AvisoAdeudoAtencionModel, AvisoAdeudoEntregaModel, UsuarioNoRegistradoModel } from '../models/AvisoAdeudo.model';

@Injectable({ providedIn: 'root' })
export class AvisoAdeudoService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/avisoAdeudo`;

  getCandidatos(): Observable<any> {
    return this.http.get(`${this.baseUrl}/candidatos`);
  }

  // Solo los candidatos de una calle -- para no calcular el adeudo de
  // TODOS los usuarios activos cada vez que se abre la pantalla (lento).
  // Se pide hasta que el usuario elige zona y calle.
  getCandidatosPorCalle(calleId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/candidatos/porCalle/${calleId}`);
  }

  getHistorial(): Observable<any> {
    return this.http.get(`${this.baseUrl}/historial`);
  }

  // Recalcula el estado ACTUAL de un solo usuario (no se confía en lo que
  // diga una fila vieja del historial) -- para el botón "Generar Segundo
  // aviso" directo desde el Primer aviso. Regresa el mismo shape que
  // getCandidatos()/getCandidatosPorCalle() (data: lista de 0 o 1 usuario).
  getCandidatoUnico(aguaUsuarioId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/candidatoUnico/${aguaUsuarioId}`);
  }

  // Se pide la respuesta completa (no solo el body): el PDF viene en el
  // cuerpo, pero los avisos -- usuarios omitidos por ya no tener adeudo, o
  // el motivo de un error -- van en headers propios, porque el cuerpo ya
  // está ocupado por el PDF (o vacío, en caso de error). Ver
  // AvisoAdeudoController.generar() en el backend.
  generar(
    aguaUsuarioIds: number[],
    tipoAviso: string,
    fechaPresentacion: string,
    noRegistrados: UsuarioNoRegistradoModel[] = []
  ): Observable<HttpResponse<Blob>> {
    return this.http.post(`${this.baseUrl}/generar`, { aguaUsuarioIds, tipoAviso, fechaPresentacion, noRegistrados }, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  marcarEntregada(avisoAdeudoId: number, datos: AvisoAdeudoEntregaModel): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoAdeudoId}/entregar`, datos);
  }

  cancelar(avisoAdeudoId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoAdeudoId}/cancelar`, {});
  }

  reactivar(avisoAdeudoId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoAdeudoId}/reactivar`, {});
  }

  // Cartas ya entregadas a este usuario que aún no se marcan como
  // atendidas -- para la alerta al consultar su ficha.
  getPendientesDeAtencion(aguaUsuarioId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/pendientesDeAtencion/${aguaUsuarioId}`);
  }

  marcarAtendida(avisoAdeudoId: number, datos: AvisoAdeudoAtencionModel): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoAdeudoId}/atender`, datos);
  }
}
