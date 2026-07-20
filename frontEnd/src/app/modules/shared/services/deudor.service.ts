import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DeudorService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/deudores`;

  // anio es opcional: si no se manda, el backend usa el año actual.
  getDeudores(anio?: number): Observable<any> {
    const query = anio ? `?anio=${anio}` : '';
    return this.http.get(`${this.baseUrl}/${query}`);
  }
}
