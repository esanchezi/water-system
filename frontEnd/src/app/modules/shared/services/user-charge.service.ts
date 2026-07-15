import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

const BASE = `${environment.apiUrl}/waterUserCharge`;

@Injectable({
  providedIn: 'root'
})
export class UserChargeService {

  private readonly http = inject(HttpClient);

  getChargesByUser(noUser: any): Observable<any> {
    return this.http.get(`${BASE}/${noUser}`);
  }

  getPendientesByUser(noUser: any): Observable<any> {
    return this.http.get(`${BASE}/${noUser}/pendientes`);
  }

  saveCharge(body: any): Observable<any> {
    return this.http.post(`${BASE}/`, body);
  }

  // Registra un abono (parcial o total) de un cargo ya existente.
  addPayment(aguaUsuarioCargoId: number, body: any): Observable<any> {
    return this.http.post(`${BASE}/${aguaUsuarioCargoId}/pagos`, body);
  }
}
