import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { PreregistroUsuarioCreateModel } from '../models/PreregistroUsuario.model';

const BASE = `${environment.apiUrl}/preregistro`;

@Injectable({
  providedIn: 'root'
})
export class PreregistroUsuarioService {

  private readonly http = inject(HttpClient);

  getByCasaId(casaId: number): Observable<any> {
    return this.http.get(`${BASE}/casa/${casaId}`);
  }

  create(casaId: number, body: PreregistroUsuarioCreateModel): Observable<any> {
    return this.http.post(`${BASE}/${casaId}`, body);
  }

  update(preregistroId: number, body: PreregistroUsuarioCreateModel): Observable<any> {
    return this.http.put(`${BASE}/${preregistroId}`, body);
  }

  marcarConvertido(preregistroId: number, aguaUsuarioId: number): Observable<any> {
    return this.http.put(`${BASE}/${preregistroId}/convertir/${aguaUsuarioId}`, {});
  }

  marcarDescartado(preregistroId: number, motivo?: string): Observable<any> {
    const params = motivo ? `?motivo=${encodeURIComponent(motivo)}` : '';
    return this.http.put(`${BASE}/${preregistroId}/descartar${params}`, {});
  }
}
