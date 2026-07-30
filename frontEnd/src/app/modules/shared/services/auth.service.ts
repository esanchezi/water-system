import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from 'src/environments/environment';

const BASE = `${environment.apiUrl}/auth`;
const TOKEN_KEY = 'watersystem_token';
const USERNAME_KEY = 'watersystem_username';
const NOMBRE_KEY = 'watersystem_nombre';

interface LoginResponse {
  token: string;
  username: string;
  nombre: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${BASE}/login`, { username, password }).pipe(
      tap((resp) => {
        localStorage.setItem(TOKEN_KEY, resp.token);
        localStorage.setItem(USERNAME_KEY, resp.username);
        localStorage.setItem(NOMBRE_KEY, resp.nombre || '');
      })
    );
  }

  cambiarPassword(passwordActual: string, passwordNueva: string): Observable<any> {
    return this.http.put(`${BASE}/cambiar-password`, { passwordActual, passwordNueva });
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
    localStorage.removeItem(NOMBRE_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getUsername(): string | null {
    return localStorage.getItem(USERNAME_KEY);
  }

  getNombre(): string | null {
    return localStorage.getItem(NOMBRE_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
