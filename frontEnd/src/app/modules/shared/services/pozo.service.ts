import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { PozoModel } from '../models/WaterValves.model';

@Injectable({ providedIn: 'root' })
export class PozoService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/pozo`;

  getListPozo(): Observable<any> {
    return this.http.get(`${this.baseUrl}/`);
  }

  getPozoById(pozoId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${pozoId}`);
  }

  addPozo(pozo: Partial<PozoModel>): Observable<any> {
    return this.http.post(`${this.baseUrl}/`, pozo);
  }

  updatePozo(pozoId: number, pozo: Partial<PozoModel>): Observable<any> {
    return this.http.put(`${this.baseUrl}/${pozoId}`, pozo);
  }

  deletePozo(pozoId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${pozoId}`);
  }
}
