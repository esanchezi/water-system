import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { SistemaUsuarioCreateModel, SistemaUsuarioModel } from '../models/SistemaUsuario.model';

@Injectable({ providedIn: 'root' })
export class SistemaUsuarioService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/sistemaUsuario`;

  getAll(): Observable<any> {
    return this.http.get(`${this.baseUrl}/`);
  }

  create(dto: SistemaUsuarioCreateModel): Observable<any> {
    return this.http.post(`${this.baseUrl}/`, dto);
  }

  update(id: number, dto: Partial<SistemaUsuarioModel>): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, dto);
  }

  resetPassword(id: number, passwordNueva: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/resetPassword`, { passwordNueva });
  }

  deactivate(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/deactivate`, {});
  }
}
