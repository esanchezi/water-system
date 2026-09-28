import { Component, OnInit, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import Swal from 'sweetalert2';
import { SistemaUsuarioService } from '../../../shared/services/sistema-usuario.service';
import { ROLES_SISTEMA, SistemaUsuarioModel } from '../../../shared/models/SistemaUsuario.model';

// Administración de cuentas de acceso (login) -- distinto de "Usuarios"
// (que administra a los clientes del servicio de agua). Pantalla nueva
// para que la propia administradora pueda dar de alta cuentas con rol
// restringido (ej. "USUARIO1", sin las secciones de Finanzas) sin
// depender de pedirlo cada vez. Solo visible para rol ADMIN (ver sidenav).
@Component({
  selector: 'app-sistema-usuario-list',
  templateUrl: './sistema-usuario-list.component.html',
  styleUrls: ['./sistema-usuario-list.component.css']
})
export class SistemaUsuarioListComponent implements OnInit {

  private readonly sistemaUsuarioService = inject(SistemaUsuarioService);
  private readonly snackBar = inject(MatSnackBar);

  readonly roles = ROLES_SISTEMA;

  cuentas: SistemaUsuarioModel[] = [];
  cargando = false;
  guardando = false;

  nuevoUsername = '';
  nuevoPassword = '';
  nuevoNombre = '';
  nuevoRol: string | null = null;

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.cargando = true;
    this.sistemaUsuarioService.getAll().subscribe({
      next: (resp: any) => {
        this.cuentas = resp?.data ?? [];
        this.cargando = false;
      },
      error: (e) => {
        console.error(e);
        this.cargando = false;
        this.openSnackBar('No se pudieron cargar las cuentas', 'Error');
      }
    });
  }

  crearCuenta(): void {
    if (!this.nuevoUsername.trim()) {
      this.openSnackBar('Ponle un usuario a la cuenta', 'Atención');
      return;
    }
    if (!this.nuevoPassword || this.nuevoPassword.length < 8) {
      this.openSnackBar('La contraseña debe tener al menos 8 caracteres', 'Atención');
      return;
    }
    if (!this.nuevoRol) {
      this.openSnackBar('Elige un rol para la cuenta', 'Atención');
      return;
    }
    this.guardando = true;
    this.sistemaUsuarioService.create({
      username: this.nuevoUsername.trim(),
      password: this.nuevoPassword,
      nombre: this.nuevoNombre.trim(),
      rol: this.nuevoRol
    }).subscribe({
      next: (resp: any) => {
        this.guardando = false;
        if (resp?.metadata?.code === '00') {
          this.openSnackBar('Cuenta creada', 'Éxito');
          this.nuevoUsername = '';
          this.nuevoPassword = '';
          this.nuevoNombre = '';
          this.nuevoRol = null;
          this.cargar();
        } else {
          this.openSnackBar(resp?.metadata?.message || 'No se pudo crear la cuenta', 'Error');
        }
      },
      error: (e) => {
        this.guardando = false;
        console.error(e);
        this.openSnackBar(e?.error?.metadata?.message || 'No se pudo crear la cuenta', 'Error');
      }
    });
  }

  editarCuenta(cuenta: SistemaUsuarioModel): void {
    const opcionesRol = this.roles.map(r => `<option value="${r}" ${r === cuenta.rol ? 'selected' : ''}>${r}</option>`).join('');
    Swal.fire({
      title: `Editar "${cuenta.username}"`,
      html:
        `<input id="swal-nombre" class="swal2-input" placeholder="Nombre" value="${cuenta.nombre ?? ''}">` +
        `<select id="swal-rol" class="swal2-input">${opcionesRol}</select>`,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: 'Guardar',
      cancelButtonText: 'Cancelar',
      preConfirm: () => {
        const nombre = (document.getElementById('swal-nombre') as HTMLInputElement)?.value || '';
        const rol = (document.getElementById('swal-rol') as HTMLSelectElement)?.value;
        return { nombre, rol };
      }
    }).then(result => {
      if (!result.isConfirmed || !result.value || !cuenta.sistemaUsuarioId) {
        return;
      }
      this.sistemaUsuarioService.update(cuenta.sistemaUsuarioId, result.value).subscribe({
        next: () => {
          this.openSnackBar('Cuenta actualizada', 'Éxito');
          this.cargar();
        },
        error: (e) => {
          console.error(e);
          this.openSnackBar('No se pudo actualizar la cuenta', 'Error');
        }
      });
    });
  }

  resetearPassword(cuenta: SistemaUsuarioModel): void {
    Swal.fire({
      title: `Restablecer contraseña de "${cuenta.username}"`,
      input: 'password',
      inputLabel: 'Nueva contraseña (mínimo 8 caracteres)',
      showCancelButton: true,
      confirmButtonText: 'Restablecer',
      cancelButtonText: 'Cancelar',
      inputValidator: (value) => (!value || value.length < 8) ? 'Mínimo 8 caracteres' : undefined
    }).then(result => {
      if (!result.isConfirmed || !cuenta.sistemaUsuarioId) {
        return;
      }
      this.sistemaUsuarioService.resetPassword(cuenta.sistemaUsuarioId, result.value).subscribe({
        next: () => this.openSnackBar('Contraseña restablecida', 'Éxito'),
        error: (e) => {
          console.error(e);
          this.openSnackBar('No se pudo restablecer la contraseña', 'Error');
        }
      });
    });
  }

  desactivarCuenta(cuenta: SistemaUsuarioModel): void {
    Swal.fire({
      title: `¿Desactivar la cuenta "${cuenta.username}"?`,
      text: 'Ya no va a poder iniciar sesión.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, desactivar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed || !cuenta.sistemaUsuarioId) {
        return;
      }
      this.sistemaUsuarioService.deactivate(cuenta.sistemaUsuarioId).subscribe({
        next: () => {
          this.openSnackBar('Cuenta desactivada', 'Éxito');
          this.cargar();
        },
        error: (e) => {
          console.error(e);
          this.openSnackBar('No se pudo desactivar la cuenta', 'Error');
        }
      });
    });
  }

  private openSnackBar(message: string, action: string): void {
    this.snackBar.open(message, action, { duration: 3000 });
  }
}
