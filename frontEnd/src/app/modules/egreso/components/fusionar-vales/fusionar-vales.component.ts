import { Component, Inject, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { WaterEgresoService } from 'src/app/modules/shared/services/water-egreso.service';

export interface FusionarValesDialogData {
  valeIds: number[];
  totalSeleccionado: number;
}

// Fusiona varios vales YA EMITIDOS (cada uno con su propio folio) en un
// vale nuevo más grande -- ej. el vale de nómina de una persona + el de
// otra, juntos en "Pago de nómina del mes". Cada vale seleccionado pasa a
// ser línea del vale nuevo; sus propias sub-líneas (si tenía) no se tocan.
@Component({
  selector: 'app-fusionar-vales',
  templateUrl: './fusionar-vales.component.html',
  styleUrls: ['./fusionar-vales.component.css']
})
export class FusionarValesComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly egresoService = inject(WaterEgresoService);
  private readonly catalogService = inject(CatalogService);

  public valeForm!: FormGroup;
  tiposComprobante: CatalogOptionModel[] = [];

  constructor(
    public dialogRef: MatDialogRef<FusionarValesComponent>,
    @Inject(MAT_DIALOG_DATA) public data: FusionarValesDialogData
  ) { }

  ngOnInit(): void {
    this.valeForm = this.fb.group({
      fechaPago:          ['', Validators.required],
      tipoComprobanteId:  [''],
      noFolio:            [''],
      descripcion:        [''],
      justificacion:      ['']
    });

    this.catalogService.getOptionsByClave('TIPO_COMPROBANTE_EGRESO').subscribe({
      next: (opts) => this.tiposComprobante = opts,
      error: (e: any) => console.error(e)
    });
  }

  onSave(): void {
    if (this.valeForm.invalid || !this.data.valeIds.length) return;

    const form = this.valeForm.value;
    const body = {
      fechaPago:          form.fechaPago,
      tipoComprobanteId:  form.tipoComprobanteId || null,
      noFolio:            form.noFolio || null,
      descripcion:        form.descripcion || null,
      justificacion:      form.justificacion || null,
      valeIds:            this.data.valeIds
    };

    this.egresoService.fusionarVales(body).subscribe({
      next: () => this.dialogRef.close(1),
      error: () => this.dialogRef.close(2)
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
