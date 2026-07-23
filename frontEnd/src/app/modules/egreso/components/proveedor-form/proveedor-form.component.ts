import { Component, Inject, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { PersonModel } from 'src/app/modules/shared/models/Person.model';
import { PersonService } from 'src/app/modules/shared/services/person.service';

export interface ProveedorFormDialogData {
  persona?: PersonModel;
}

// Alta/edición rápida de un proveedor (persona) sin salir del flujo de
// captura de gastos: si data.persona viene con personaId, se actualiza esa
// persona; si no, se crea una nueva. Reutiliza el mismo endpoint que ya usa
// el módulo de Personas (POST /setPerson vía PersonService.savePersonData).
@Component({
  selector: 'app-proveedor-form',
  templateUrl: './proveedor-form.component.html',
  styleUrls: ['./proveedor-form.component.css']
})
export class ProveedorFormComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly personService = inject(PersonService);

  form!: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<ProveedorFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ProveedorFormDialogData
  ) { }

  get esEdicion(): boolean {
    return !!this.data?.persona?.personaId;
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      personaId: [this.data?.persona?.personaId || null],
      nombre:    [this.data?.persona?.nombre || ''],
      nombre2:   [this.data?.persona?.nombre2 || ''],
      app:       [this.data?.persona?.app || '', Validators.required],
      apm:       [this.data?.persona?.apm || '']
    });
  }

  onSave(): void {
    if (this.form.invalid) return;

    this.personService.savePersonData(this.form.value).subscribe({
      next: (resp: any) => this.dialogRef.close(resp as PersonModel),
      error: () => this.dialogRef.close(null)
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
