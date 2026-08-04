import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { AuthService } from '../../../shared/services/auth.service';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {

  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly dialogRef = inject(MatDialogRef<ChangePasswordComponent>);

  form: FormGroup = this.fb.group({
    passwordActual: ['', Validators.required],
    passwordNueva: ['', [Validators.required, Validators.minLength(8)]],
    confirmar: ['', Validators.required]
  });

  error = '';
  guardando = false;

  onSave(): void {
    if (this.form.invalid) return;
    const { passwordActual, passwordNueva, confirmar } = this.form.value;
    if (passwordNueva !== confirmar) {
      this.error = 'La confirmación no coincide con la nueva contraseña.';
      return;
    }

    this.guardando = true;
    this.error = '';
    this.authService.cambiarPassword(passwordActual, passwordNueva).subscribe({
      next: () => {
        this.guardando = false;
        this.dialogRef.close(true);
      },
      error: (e: any) => {
        this.guardando = false;
        this.error = e?.error || 'No se pudo cambiar la contraseña.';
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
