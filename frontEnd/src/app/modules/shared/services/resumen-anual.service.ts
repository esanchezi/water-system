import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ResumenAnualService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/resumenAnual`;

  getResumenAnual(): Observable<any> {
    return this.http.get(`${this.baseUrl}/`);
  }
}
