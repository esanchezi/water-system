import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { WaterGroupModel } from '../models/WaterUser.model';


@Injectable({providedIn: 'root'})
export class GroupService {

  private readonly baseUrl = 'http://localhost:8080/Los_Lopez/api/v1/waterGroup'; // 🔹

  constructor(private http:HttpClient) {}

  getListWaterGroup(){
    const endpoint = `${this.baseUrl}/`;
    return this.http.get(endpoint);
  }

  addGroup(house: Partial<WaterGroupModel>): Observable<WaterGroupModel> {
    return this.http.post<WaterGroupModel>(`${this.baseUrl}/`, house);
  }

  updateWaterGroup(casaId: number, request: WaterGroupModel): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${casaId}`, request);
  }

}
