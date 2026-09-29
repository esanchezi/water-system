import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class AvisoInformativoAdeudoFotoService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/avisoInformativoAdeudo`;

  subirFoto(avisoInformativoAdeudoId: number, archivo: File): Observable<any> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post(`${this.baseUrl}/${avisoInformativoAdeudoId}/fotos`, formData);
  }

  listarPorAviso(avisoInformativoAdeudoId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${avisoInformativoAdeudoId}/fotos`);
  }

  eliminarFoto(fotoId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/fotos/${fotoId}`);
  }

  getArchivoBlob(fotoId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/fotos/${fotoId}/archivo`, { responseType: 'blob' });
  }
}
