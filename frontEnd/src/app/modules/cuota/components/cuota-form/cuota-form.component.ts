import { Component, inject, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { FeeService } from 'src/app/modules/shared/services/fee.service';
import { CatalogModel } from 'src/app/modules/shared/models/Catalog.model';
import { FeeModel } from 'src/app/modules/shared/models/Fee.model';

@Component({
  selector: 'app-cuota-form',
  templateUrl: './cuota-form.component.html',
  styleUrls: ['./cuota-form.component.css']
})
export class CuotaFormComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);
  private readonly feeService = inject(FeeService);
  private readonly dialogRef = inject(MatDialogRef<CuotaFormComponent>);

  form!: FormGroup;
  isEdit = false;
  saving = false;

  // Se eligen de forma genérica: primero el catálogo, luego la opción dentro
  // de ese catálogo. Así no se asume un id fijo para "Uso" o "Tipo de usuario".
  catalogos: CatalogModel[] = [];
  opcionesUso: CatalogModel['options'] = [];
  opcionesTipo: CatalogModel['options'] = [];

  constructor(@Inject(MAT_DIALOG_DATA) public data: FeeModel | null) {}

  ngOnInit(): void {
    this.isEdit = !!this.data;
    this.form = this.fb.group({
      catalogoUsoId:  [null, Validators.required],
      usoId:          [this.data?.uso?.catalogoOpcionesId ?? null, Validators.required],
      catalogoTipoId: [null, Validators.required],
      userTypeId:     [this.data?.userType?.catalogoOpcionesId ?? null, Validators.required],
      observaciones:  [this.data?.observaciones ?? '']
    });

    this.catalogService.getAll().subscribe({
      next: (cats) => {
        this.catalogos = cats;
        this.preseleccionar(cats);
      },
      error: (e: any) => console.error(e)
    });
  }

  // Al editar, busca a qué catálogo pertenece la opción ya asignada para
  // dejar los selects preseleccionados.
  private preseleccionar(cats: CatalogModel[]): void {
    if (this.data?.uso) {
      const catUso = cats.find(c => c.options?.some(o => o.catalogoOpcionesId === this.data!.uso.catalogoOpcionesId));
      if (catUso) {
        this.form.patchValue({ catalogoUsoId: catUso.catalogoId });
        this.opcionesUso = catUso.options;
      }
    }
    if (this.data?.userType) {
      const catTipo = cats.find(c => c.options?.some(o => o.catalogoOpcionesId === this.data!.userType.catalogoOpcionesId));
      if (catTipo) {
        this.form.patchValue({ catalogoTipoId: catTipo.catalogoId });
        this.opcionesTipo = catTipo.options;
      }
    }
  }

  onCatalogoUsoChange(catalogoId: number): void {
    this.opcionesUso = this.catalogos.find(c => c.catalogoId === catalogoId)?.options ?? [];
    this.form.patchValue({ usoId: null });
  }

  onCatalogoTipoChange(catalogoId: number): void {
    this.opcionesTipo = this.catalogos.find(c => c.catalogoId === catalogoId)?.options ?? [];
    this.form.patchValue({ userTypeId: null });
  }

  save(): void {
    if (this.form.invalid) return;
    this.saving = true;

    const body = {
      usoId:         this.form.value.usoId,
      userTypeId:    this.form.value.userTypeId,
      observaciones: this.form.value.observaciones
    };

    const request$ = this.isEdit
      ? this.feeService.update(this.data!.cuotaId, body)
      : this.feeService.create(body);

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
