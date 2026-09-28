import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { ValorGeneralCrearModel } from '../models/ValorGeneral.model';

const base_url = `${environment.apiUrl}/valorGeneral`;

@Injectable({
  providedIn: 'root'
})
export class ValorGeneralService {

  private readonly http = inject(HttpClient);

  getAll(): Observable<any> {
    return this.http.get(`${base_url}/`);
  }

  create(body: ValorGeneralCrearModel): Observable<any> {
    return this.http.post(`${base_url}/`, body);
  }

  update(valorGeneralId: number, body: ValorGeneralCrearModel): Observable<any> {
    return this.http.put(`${base_url}/${valorGeneralId}`, body);
  }

  deactivate(valorGeneralId: number): Observable<any> {
    return this.http.put(`${base_url}/${valorGeneralId}/baja`, {});
  }
}
