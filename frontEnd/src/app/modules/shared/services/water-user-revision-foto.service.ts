import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class WaterUserRevisionFotoService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/waterUserRevision`;

  subirFoto(revisionId: number, archivo: File): Observable<any> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post(`${this.baseUrl}/${revisionId}/fotos`, formData);
  }

  listarPorRevision(revisionId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${revisionId}/fotos`);
  }

  eliminarFoto(fotoId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/fotos/${fotoId}`);
  }

  // El endpoint del archivo pide JWT, así que no puede ser un [src] directo
  // -- se pide como blob y se convierte a una URL local para mostrarlo.
  getArchivoBlob(fotoId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/fotos/${fotoId}/archivo`, { responseType: 'blob' });
  }
}
