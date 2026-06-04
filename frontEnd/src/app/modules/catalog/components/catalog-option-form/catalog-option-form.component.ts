import { Component, inject, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CatalogService } from '../../../shared/services/catalog.service';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';

export interface OptionDialogData {
  catalogoId: number;
  option: CatalogOptionModel | null;
}

@Component({
  selector: 'app-catalog-option-form',
  templateUrl: './catalog-option-form.component.html',
  styleUrls: ['./catalog-option-form.component.css']
})
export class CatalogOptionFormComponent implements OnInit {

  private readonly fb      = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);
  private readonly dialogRef = inject(MatDialogRef<CatalogOptionFormComponent>);

  form!: FormGroup;
  isEdit = false;
  saving = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: OptionDialogData) {}

  ngOnInit(): void {
    this.isEdit = !!this.data.option;
    this.form = this.fb.group({
      nombre:      [this.data.option?.nombre ?? '', [Validators.required, Validators.maxLength(255)]],
      descripcion: [this.data.option?.descripcion ?? '', Validators.maxLength(255)],
    });
  }

  save(): void {
    if (this.form.invalid) return;
    this.saving = true;

    const body = this.form.value;
    const { catalogoId, option } = this.data;

    const request$ = this.isEdit
      ? this.catalogService.updateOption(catalogoId, option!.catalogoOpcionesId, body)
      : this.catalogService.addOption(catalogoId, body);

    request$.subscribe({
      next: () => {
        this.saving = false;
        this.dialogRef.close(true);
      },
      error: (e) => {
        console.error(e);
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
