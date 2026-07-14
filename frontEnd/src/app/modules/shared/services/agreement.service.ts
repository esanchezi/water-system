import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

const BASE = `${environment.apiUrl}/waterAgreement`;

@Injectable({
  providedIn: 'root'
})
export class AgreementService {

  private readonly http = inject(HttpClient);

  getAll(): Observable<any> {
    return this.http.get(`${BASE}/`);
  }

  getByNoUser(noUser: any): Observable<any> {
    return this.http.get(`${BASE}/${noUser}`);
  }

  save(body: any): Observable<any> {
    return this.http.post(`${BASE}/`, body);
  }
}
