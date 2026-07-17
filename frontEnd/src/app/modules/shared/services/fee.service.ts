import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { FeeAmountCreateModel, FeeCreateModel } from '../models/Fee.model';

const BASE = `${environment.apiUrl}/fee`;

@Injectable({
  providedIn: 'root'
})
export class FeeService {

  private readonly http = inject(HttpClient);

  getFeeAmount(): Observable<any> {
    return this.http.get(`${BASE}/`);
  }

  create(body: FeeCreateModel): Observable<any> {
    return this.http.post(`${BASE}/`, body);
  }

  update(cuotaId: number, body: FeeCreateModel): Observable<any> {
    return this.http.put(`${BASE}/${cuotaId}`, body);
  }

  addAmount(cuotaId: number, body: FeeAmountCreateModel): Observable<any> {
    return this.http.post(`${BASE}/${cuotaId}/amounts`, body);
  }

  updateAmount(cuotaId: number, cuotaMontoId: number, body: FeeAmountCreateModel): Observable<any> {
    return this.http.put(`${BASE}/${cuotaId}/amounts/${cuotaMontoId}`, body);
  }

  deactivate(cuotaId: number): Observable<any> {
    return this.http.put(`${BASE}/${cuotaId}/deactivate`, {});
  }

  deactivateAmount(cuotaId: number, cuotaMontoId: number): Observable<any> {
    return this.http.put(`${BASE}/${cuotaId}/amounts/${cuotaMontoId}/deactivate`, {});
  }
}
