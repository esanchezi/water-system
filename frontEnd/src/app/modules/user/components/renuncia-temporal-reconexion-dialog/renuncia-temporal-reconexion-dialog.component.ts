import { Component, Inject, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { RenunciaTemporalModel } from 'src/app/modules/shared/models/RenunciaTemporal.model';
import { RenunciaTemporalService } from 'src/app/modules/shared/services/renuncia-temporal.service';

export interface RenunciaTemporalReconexionDialogData {
  renuncia: RenunciaTemporalModel;
}

// Cierra la renuncia temporal: registra la reconexión conforme al
// Art. 32 (requiere solicitud + condiciones que autoriza la asamblea).
// En cuanto se guarda, fechaReconexion deja de ser null y el usuario
// vuelve a ser candidato normal a carta de adeudo.
@Component({
  selector: 'app-renuncia-temporal-reconexion-dialog',
  templateUrl: './renuncia-temporal-reconexion-dialog.component.html',
  styleUrls: ['./renuncia-temporal-reconexion-dialog.component.css']
})
export class RenunciaTemporalReconexionDialogComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly renunciaTemporalService = inject(RenunciaTemporalService);

  public form!: FormGroup;
  guardando = false;

  constructor(
    public dialogRef: MatDialogRef<RenunciaTemporalReconexionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: RenunciaTemporalReconexionDialogData
  ) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      fechaSolicitudReconexion: ['', Validators.required],
      fechaAsamblea:            [''],
      condicionesReconexion:    ['']
    });
  }

  onGuardar(): void {
    if (this.form.invalid) return;
    const form = this.form.value;
    this.guardando = true;
    this.renunciaTemporalService.reconectar(this.data.renuncia.renunciaTemporalId, {
      fechaSolicitudReconexion: form.fechaSolicitudReconexion || null,
      fechaAsamblea:            form.fechaAsamblea || null,
      condicionesReconexion:    form.condicionesReconexion || null
    }).subscribe({
      next: () => this.dialogRef.close(1),
      error: (e: any) => {
        console.error(e);
        this.guardando = false;
        this.dialogRef.close(2);
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
