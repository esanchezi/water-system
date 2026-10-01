import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class AvisoPadronFotoService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/avisoPadron`;

  subirFoto(avisoPadronId: number, archivo: File): Observable<any> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post(`${this.baseUrl}/${avisoPadronId}/fotos`, formData);
  }

  listarPorAviso(avisoPadronId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${avisoPadronId}/fotos`);
  }

  eliminarFoto(fotoId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/fotos/${fotoId}`);
  }

  // Mismo motivo que AvisoAdeudoFotoService.getArchivoBlob(): el endpoint
  // del archivo pide JWT, así que no puede ser un [src] directo -- se pide
  // como blob y se convierte a una URL local para mostrarlo.
  getArchivoBlob(fotoId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/fotos/${fotoId}/archivo`, { responseType: 'blob' });
  }
}
