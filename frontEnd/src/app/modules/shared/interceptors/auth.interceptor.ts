import { Injectable, inject } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

// Agrega el token a cada petición hacia la API, y si el backend responde
// 401 (token inválido/expirado), cierra la sesión local y regresa a login
// -- sin esto, una sesión vencida se quedaría mostrando pantallas rotas en
// vez de mandar a capturar credenciales de nuevo.
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    const isAuthCall = req.url.includes('/auth/login');

    const authReq = (token && !isAuthCall)
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

    return next.handle(authReq).pipe(
      catchError((error) => {
        if (error?.status === 401 && !isAuthCall) {
          this.authService.logout();
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }
}
