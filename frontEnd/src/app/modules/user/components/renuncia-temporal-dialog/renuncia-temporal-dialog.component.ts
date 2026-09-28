import { Component, Inject, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { RenunciaTemporalService } from 'src/app/modules/shared/services/renuncia-temporal.service';

export interface RenunciaTemporalDialogData {
  aguaUsuarioId: number;
  noUsuario: number;
  nombreUsuario: string;
}

// Solicita la renuncia temporal al servicio (Art. 6 Bis) -- el backend
// calcula el adeudo a la fecha, asigna folio y genera el PDF de la
// solicitud/acta, que se descarga en cuanto se guarda. dialogRef.close()
// regresa el Blob del PDF (para que la ficha lo descargue) o null si se
// canceló / hubo error.
@Component({
  selector: 'app-renuncia-temporal-dialog',
  templateUrl: './renuncia-temporal-dialog.component.html',
  styleUrls: ['./renuncia-temporal-dialog.component.css']
})
export class RenunciaTemporalDialogComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly renunciaTemporalService = inject(RenunciaTemporalService);

  public form!: FormGroup;
  guardando = false;

  constructor(
    public dialogRef: MatDialogRef<RenunciaTemporalDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: RenunciaTemporalDialogData
  ) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      fechaRenuncia: [new Date().toISOString().substring(0, 10), Validators.required],
      motivo:        ['', Validators.required]
    });
  }

  onGuardar(): void {
    if (this.form.invalid) return;
    const form = this.form.value;
    this.guardando = true;
    this.renunciaTemporalService.generar({
      aguaUsuarioId: this.data.aguaUsuarioId,
      fechaRenuncia: form.fechaRenuncia,
      motivo:        form.motivo
    }).subscribe({
      next: (resp) => {
        this.guardando = false;
        this.dialogRef.close({ ok: true, blob: resp.body });
      },
      error: (e: any) => {
        this.guardando = false;
        console.error(e);
        this.dialogRef.close({ ok: false, mensaje: e?.headers?.get?.('X-Error-Message') });
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
