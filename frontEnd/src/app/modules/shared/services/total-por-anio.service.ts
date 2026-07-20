import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TotalPorAnioService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/totalPorAnio`;

  getTotalesPorAnio(): Observable<any> {
    return this.http.get(`${this.baseUrl}/`);
  }
}
