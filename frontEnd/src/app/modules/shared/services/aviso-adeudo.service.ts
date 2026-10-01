import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AvisoAdeudoAtencionModel, AvisoAdeudoEntregaModel, UsuarioManualAdeudoModel, UsuarioNoRegistradoModel } from '../models/AvisoAdeudo.model';

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

  // Historial completo (activas + canceladas) de un usuario específico --
  // para el acordeón "Cartas generadas" en su ficha.
  getPorUsuario(aguaUsuarioId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/porUsuario/${aguaUsuarioId}`);
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
    noRegistrados: UsuarioNoRegistradoModel[] = [],
    usuariosManuales: UsuarioManualAdeudoModel[] = []
  ): Observable<HttpResponse<Blob>> {
    // El backend solo espera aguaUsuarioId/montoAdeudo/observacion/
    // calcularAutomatico -- se limpian los campos que solo son para mostrar
    // en la tabla del frontend (noUsuario, nombreCompleto).
    const usuariosManualesPayload = usuariosManuales.map(u => ({
      aguaUsuarioId: u.aguaUsuarioId,
      montoAdeudo: u.montoAdeudo,
      observacion: u.observacion,
      calcularAutomatico: u.calcularAutomatico
    }));
    return this.http.post(`${this.baseUrl}/generar`,
      { aguaUsuarioIds, tipoAviso, fechaPresentacion, noRegistrados, usuariosManuales: usuariosManualesPayload }, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  marcarEntregada(avisoAdeudoId: number, datos: AvisoAdeudoEntregaModel): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoAdeudoId}/entregar`, datos);
  }

  // comentarioCancela es opcional -- sobre todo para dejar constancia del
  // motivo cuando se cancela porque la carta no fue entregada/recibida.
  cancelar(avisoAdeudoId: number, comentarioCancela?: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${avisoAdeudoId}/cancelar`, { comentarioCancela });
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
