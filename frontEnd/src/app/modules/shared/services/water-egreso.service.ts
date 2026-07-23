import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

const BASE = `${environment.apiUrl}/waterEgreso`;

@Injectable({
  providedIn: 'root'
})
export class WaterEgresoService {

  private readonly http = inject(HttpClient);

  getAll(): Observable<any> {
    return this.http.get(`${BASE}/`);
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${BASE}/${id}`);
  }

  save(body: any): Observable<any> {
    return this.http.post(`${BASE}/`, body);
  }

  deactivate(id: number): Observable<any> {
    return this.http.put(`${BASE}/${id}/deactivate`, {});
  }

  // Actualiza (parcial) revisado/validadoFisico de un vale.
  marcar(id: number, body: { revisado?: boolean; validadoFisico?: boolean }): Observable<any> {
    return this.http.put(`${BASE}/${id}/marcar`, body);
  }

  // Captura de un gasto suelto (todavía sin vale).
  crearGasto(body: any): Observable<any> {
    return this.http.post(`${BASE}/gasto`, body);
  }

  // Corrige un gasto que sigue pendiente (aún no incluido en un vale).
  actualizarGasto(id: number, body: any): Observable<any> {
    return this.http.put(`${BASE}/gasto/${id}`, body);
  }

  // Gastos sueltos pendientes de incluirse en un vale.
  getPendientes(): Observable<any> {
    return this.http.get(`${BASE}/pendientes`);
  }

  // Junta los gastos pendientes seleccionados en un solo vale.
  emitirVale(body: any): Observable<any> {
    return this.http.post(`${BASE}/emitir`, body);
  }
}
