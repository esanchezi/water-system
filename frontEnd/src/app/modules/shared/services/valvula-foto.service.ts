import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class ValvulaFotoService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/valvula`;

  subirFoto(valvulaId: number, archivo: File): Observable<any> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post(`${this.baseUrl}/${valvulaId}/fotos`, formData);
  }

  listarPorValvula(valvulaId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${valvulaId}/fotos`);
  }

  eliminarFoto(fotoId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/fotos/${fotoId}`);
  }

  // El endpoint del archivo está protegido igual que el resto de /api/v1/**
  // (necesita el token JWT), así que no se puede usar directo como
  // [src] de un <img> -- se pide como blob (el interceptor le pone el
  // Authorization igual que a cualquier otra petición) y se convierte a
  // una URL local para mostrarlo.
  getArchivoBlob(fotoId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/fotos/${fotoId}/archivo`, { responseType: 'blob' });
  }
}
