import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { WaterHouseModel } from '../models/WaterUser.model';

@Injectable({ providedIn: 'root' })
export class HouseService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/waterHouse`;

  getListWaterHouse(): Observable<any> {
    return this.http.get(`${this.baseUrl}/`);
  }

  addHouse(house: Partial<WaterHouseModel>): Observable<WaterHouseModel> {
    return this.http.post<WaterHouseModel>(`${this.baseUrl}/`, house);
  }

  updateWaterHouse(casaId: number, request: WaterHouseModel): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${casaId}`, request);
  }
}
