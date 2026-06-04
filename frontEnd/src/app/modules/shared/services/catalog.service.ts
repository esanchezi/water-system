import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { CatalogModel, CatalogOptionModel, CatalogRestResponse } from '../models/Catalog.model';

const BASE = `${environment.apiUrl}/catalog`;

@Injectable({ providedIn: 'root' })
export class CatalogService {

  private readonly http = inject(HttpClient);

  // ── Métodos nuevos CRUD ─────────────────────────────────────────────────

  getAll(): Observable<CatalogModel[]> {
    return this.http.get<CatalogRestResponse>(`${BASE}/`).pipe(
      map(resp => resp.data ?? [])
    );
  }

  getById(id: number): Observable<CatalogModel> {
    return this.http.get<CatalogRestResponse>(`${BASE}/${id}`).pipe(
      map(resp => resp.data[0])
    );
  }

  getOptions(id: number): Observable<CatalogOptionModel[]> {
    return this.getById(id).pipe(
      map(cat => cat.options ?? [])
    );
  }

  create(body: Partial<CatalogModel>): Observable<CatalogRestResponse> {
    return this.http.post<CatalogRestResponse>(`${BASE}/`, body);
  }

  update(id: number, body: Partial<CatalogModel>): Observable<CatalogRestResponse> {
    return this.http.put<CatalogRestResponse>(`${BASE}/${id}`, body);
  }

  deactivate(id: number): Observable<CatalogRestResponse> {
    return this.http.delete<CatalogRestResponse>(`${BASE}/${id}`);
  }

  addOption(catalogoId: number, body: Partial<CatalogOptionModel>): Observable<CatalogRestResponse> {
    return this.http.post<CatalogRestResponse>(`${BASE}/${catalogoId}/options`, body);
  }

  updateOption(catalogoId: number, opcionId: number, body: Partial<CatalogOptionModel>): Observable<CatalogRestResponse> {
    return this.http.put<CatalogRestResponse>(`${BASE}/${catalogoId}/options/${opcionId}`, body);
  }

  deactivateOption(catalogoId: number, opcionId: number): Observable<CatalogRestResponse> {
    return this.http.delete<CatalogRestResponse>(`${BASE}/${catalogoId}/options/${opcionId}`);
  }

  // ── Métodos legacy (compatibilidad con módulos existentes) ──────────────

  getCatalogById(id: number): Observable<CatalogRestResponse> {
    return this.http.get<CatalogRestResponse>(`${BASE}/${id}`);
  }

  getCatalogByIdResponse(id: number): Observable<CatalogRestResponse> {
    return this.http.get<CatalogRestResponse>(`${BASE}/${id}`);
  }
}
