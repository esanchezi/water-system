import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from 'src/environments/environment';
import { CLAVE_NOMBRE_COMITE, ConfiguracionModel } from '../models/Configuracion.model';

@Injectable({ providedIn: 'root' })
export class ConfiguracionService {

  private readonly http    = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/configuracion`;

  // El nombre del comité se usa en el título de la app (sidenav). Se
  // expone como observable con un valor por defecto para que no se vea
  // vacío mientras carga la primera vez que se abre la app.
  private readonly nombreComiteSubject = new BehaviorSubject<string>('Los Lopez');
  readonly nombreComite$ = this.nombreComiteSubject.asObservable();

  getAll(): Observable<any> {
    return this.http.get(`${this.baseUrl}/`).pipe(
      tap((resp: any) => {
        const nombre = (resp?.data ?? [])
          .find((c: ConfiguracionModel) => c.clave === CLAVE_NOMBRE_COMITE)?.valor;
        if (nombre) {
          this.nombreComiteSubject.next(nombre);
        }
      })
    );
  }

  updateByClave(clave: string, valor: string, descripcion?: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${clave}`, { clave, valor, descripcion });
  }
}
