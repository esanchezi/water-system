import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class ReciboRevisionService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/reciboRevision`;

  subirFoto(archivo: File, observaciones?: string): Observable<any> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    if (observaciones) {
      formData.append('observaciones', observaciones);
    }
    return this.http.post(`${this.baseUrl}/fotos`, formData);
  }

  listarFotos(): Observable<any> {
    return this.http.get(`${this.baseUrl}/fotos`);
  }

  eliminarFoto(fotoId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/fotos/${fotoId}`);
  }

  // Igual que valvula-foto.service.ts: el endpoint requiere el JWT, así
  // que se pide como blob (no como [src] directo de un <img>).
  getArchivoBlob(fotoId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/fotos/${fotoId}/archivo`, { responseType: 'blob' });
  }

  agregarRevision(fotoId: number, dto: {
    noFolioCapturado: number | null;
    noUsuarioCapturado: number | null;
    montoTexto?: string;
    observaciones?: string;
  }): Observable<any> {
    return this.http.post(`${this.baseUrl}/fotos/${fotoId}/revisiones`, dto);
  }

  actualizarRevision(revisionId: number, dto: { resultado?: string; observaciones?: string }): Observable<any> {
    return this.http.put(`${this.baseUrl}/revisiones/${revisionId}`, dto);
  }

  eliminarRevision(revisionId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/revisiones/${revisionId}`);
  }
}
