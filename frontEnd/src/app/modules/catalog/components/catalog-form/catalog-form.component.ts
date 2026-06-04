import { Component, inject, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CatalogService } from '../../../shared/services/catalog.service';
import { CatalogModel } from 'src/app/modules/shared/models/Catalog.model';

@Component({
  selector: 'app-catalog-form',
  templateUrl: './catalog-form.component.html',
  styleUrls: ['./catalog-form.component.css']
})
export class CatalogFormComponent implements OnInit {

  private readonly fb      = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);
  private readonly dialogRef = inject(MatDialogRef<CatalogFormComponent>);

  form!: FormGroup;
  isEdit = false;
  saving = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: CatalogModel | null) {}

  ngOnInit(): void {
    this.isEdit = !!this.data;
    this.form = this.fb.group({
      nombre:      [this.data?.nombre ?? '', [Validators.required, Validators.maxLength(255)]],
      descripcion: [this.data?.descripcion ?? '', Validators.maxLength(255)],
    });
  }

  save(): void {
    if (this.form.invalid) return;
    this.saving = true;

    const body = this.form.value;
    const request$ = this.isEdit
      ? this.catalogService.update(this.data!.catalogoId, body)
      : this.catalogService.create(body);

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
