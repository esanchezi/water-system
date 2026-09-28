import { Component, inject, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ValorGeneralService } from 'src/app/modules/shared/services/valor-general.service';
import { CLAVES_VALOR_GENERAL, ValorGeneralModel } from 'src/app/modules/shared/models/ValorGeneral.model';

@Component({
  selector: 'app-valor-general-form-dialog',
  templateUrl: './valor-general-form-dialog.component.html',
  styleUrls: ['./valor-general-form-dialog.component.css']
})
export class ValorGeneralFormDialogComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly valorGeneralService = inject(ValorGeneralService);
  private readonly dialogRef = inject(MatDialogRef<ValorGeneralFormDialogComponent>);

  form!: FormGroup;
  isEdit = false;
  saving = false;
  claves = CLAVES_VALOR_GENERAL;

  constructor(@Inject(MAT_DIALOG_DATA) public data: ValorGeneralModel | null) {}

  ngOnInit(): void {
    this.isEdit = !!this.data;
    this.form = this.fb.group({
      clave:         [this.data?.clave ?? null, Validators.required],
      vigencia:      [this.data?.vigencia ?? new Date().getFullYear(), [Validators.required, Validators.min(2021)]],
      monto:         [this.data?.monto ?? null, [Validators.required, Validators.min(0)]],
      observaciones: [this.data?.observaciones ?? '']
    });
  }

  save(): void {
    if (this.form.invalid) return;
    this.saving = true;

    const claveSeleccionada = this.claves.find(c => c.clave === this.form.value.clave);
    const body = {
      clave:         this.form.value.clave,
      nombre:        claveSeleccionada?.nombre ?? this.form.value.clave,
      vigencia:      this.form.value.vigencia,
      monto:         this.form.value.monto,
      observaciones: this.form.value.observaciones
    };

    const request$ = this.isEdit
      ? this.valorGeneralService.update(this.data!.valorGeneralId, body)
      : this.valorGeneralService.create(body);

    request$.subscribe({
      next: () => {
        this.saving = false;
        this.dialogRef.close(true);
      },
      error: (e: any) => {
        console.error(e);
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
