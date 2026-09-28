import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { WaterUserRevisionRequestModel } from '../models/WaterUserRevision.model';

@Injectable({ providedIn: 'root' })
export class WaterUserRevisionService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/waterUserRevision`;

  registrar(aguaUsuarioId: number, body: WaterUserRevisionRequestModel): Observable<any> {
    return this.http.post(`${this.baseUrl}/${aguaUsuarioId}`, body);
  }

  historialPorUsuario(aguaUsuarioId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/porUsuario/${aguaUsuarioId}`);
  }

  eliminar(revisionId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${revisionId}`);
  }
}
