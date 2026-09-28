import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from 'src/environments/environment';

const BASE = `${environment.apiUrl}/auth`;
const TOKEN_KEY = 'watersystem_token';
const USERNAME_KEY = 'watersystem_username';
const NOMBRE_KEY = 'watersystem_nombre';
const ROL_KEY = 'watersystem_rol';

interface LoginResponse {
  token: string;
  username: string;
  nombre: string;
  rol: string;
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
        localStorage.setItem(ROL_KEY, resp.rol || '');
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
    localStorage.removeItem(ROL_KEY);
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

  // Cuentas de antes de que existiera este campo (o cualquier caso raro
  // sin rol guardado) se tratan como ADMIN, para no dejar a nadie fuera
  // de secciones a las que ya tenía acceso.
  getRole(): string {
    return localStorage.getItem(ROL_KEY) || 'ADMIN';
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  // Antes solo revisaba que hubiera un token guardado, sin ver si ya
  // venció -- eso dejaba entrar al tablero con una sesión vieja y solo
  // hasta que fallaba la primera llamada a la API se notaba el problema
  // (pantalla "cargando" o en blanco). Ahora se revisa la fecha de
  // expiración del propio token (campo "exp") antes de dejar pasar.
  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    if (this.isTokenExpired(token)) {
      this.logout();
      return false;
    }
    return true;
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
      if (!decoded.exp) {
        return false;
      }
      return Date.now() >= decoded.exp * 1000;
    } catch {
      // Si no se puede leer el token, se trata como vencido/ inválido.
      return true;
    }
  }
}
