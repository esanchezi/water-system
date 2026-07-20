import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { WaterUserAnnualPaymentCreateModel } from '../models/WaterUserAnnualPayment.model';

const BASE = `${environment.apiUrl}/waterUserAnnualPayment`;

@Injectable({
  providedIn: 'root'
})
export class WaterUserAnnualPaymentService {

  private readonly http = inject(HttpClient);

  getByNoUser(noUser: any): Observable<any> {
    return this.http.get(`${BASE}/${noUser}`);
  }

  create(aguaUsuarioId: number, body: WaterUserAnnualPaymentCreateModel): Observable<any> {
    return this.http.post(`${BASE}/${aguaUsuarioId}`, body);
  }

  deactivate(pagoAnualId: number): Observable<any> {
    return this.http.put(`${BASE}/${pagoAnualId}/deactivate`, {});
  }
}
