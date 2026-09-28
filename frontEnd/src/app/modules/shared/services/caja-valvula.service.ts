import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { CajaValvulaModel } from '../models/WaterValves.model';

@Injectable({ providedIn: 'root' })
export class CajaValvulaService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/cajaValvula`;

  getListCajaValvula(): Observable<any> {
    return this.http.get(`${this.baseUrl}/`);
  }

  getCajaValvulaById(cajaId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${cajaId}`);
  }

  addCajaValvula(caja: Partial<CajaValvulaModel>): Observable<any> {
    return this.http.post(`${this.baseUrl}/`, caja);
  }

  updateCajaValvula(cajaId: number, caja: Partial<CajaValvulaModel>): Observable<any> {
    return this.http.put(`${this.baseUrl}/${cajaId}`, caja);
  }

  deleteCajaValvula(cajaId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${cajaId}`);
  }

  getResumen(): Observable<any> {
    return this.http.get(`${this.baseUrl}/resumen`);
  }
}
