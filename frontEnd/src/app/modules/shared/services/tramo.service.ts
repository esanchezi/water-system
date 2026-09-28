import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { TramoModel } from '../models/WaterValves.model';

@Injectable({ providedIn: 'root' })
export class TramoService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/tramo`;

  getListTramo(): Observable<any> {
    return this.http.get(`${this.baseUrl}/`);
  }

  getTramoById(tramoId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${tramoId}`);
  }

  addTramo(tramo: Partial<TramoModel>): Observable<any> {
    return this.http.post(`${this.baseUrl}/`, tramo);
  }

  updateTramo(tramoId: number, tramo: Partial<TramoModel>): Observable<any> {
    return this.http.put(`${this.baseUrl}/${tramoId}`, tramo);
  }

  deleteTramo(tramoId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${tramoId}`);
  }
}
