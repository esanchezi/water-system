import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { WaterHouseModel } from '../models/WaterUser.model';
import { Observable } from 'rxjs';


@Injectable({providedIn: 'root'})
export class HouseService {

  private readonly baseUrl = 'http://localhost:8080/Los_Lopez/api/v1/waterHouse'; // 🔹

  constructor(private http:HttpClient) {}

  getListWaterHouse(){
    const endpoint = `${this.baseUrl}/`;
    return this.http.get(endpoint);
  }

  addHouse(house: Partial<WaterHouseModel>): Observable<WaterHouseModel> {
    return this.http.post<WaterHouseModel>(`${this.baseUrl}/`, house);
  }

  updateWaterHouse(casaId: number, request: WaterHouseModel): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${casaId}`, request);
  }

}
