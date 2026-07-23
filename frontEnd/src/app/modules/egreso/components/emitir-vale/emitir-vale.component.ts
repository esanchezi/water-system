import { Component, Inject, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { WaterEgresoService } from 'src/app/modules/shared/services/water-egreso.service';

export interface EmitirValeDialogData {
  gastoIds: number[];
  totalSeleccionado: number;
}

// Último paso del flujo de gastos del mes: junta los gastos seleccionados
// (ya elegidos en la pantalla de acordeón) en un solo vale/recibo. Aquí solo
// se capturan los datos propios del comprobante que ampara ese vale.
@Component({
  selector: 'app-emitir-vale',
  templateUrl: './emitir-vale.component.html',
  styleUrls: ['./emitir-vale.component.css']
})
export class EmitirValeComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly egresoService = inject(WaterEgresoService);
  private readonly catalogService = inject(CatalogService);

  public valeForm!: FormGroup;
  tiposComprobante: CatalogOptionModel[] = [];

  constructor(
    public dialogRef: MatDialogRef<EmitirValeComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EmitirValeDialogData
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
    if (this.valeForm.invalid || !this.data.gastoIds.length) return;

    const form = this.valeForm.value;
    const body = {
      fechaPago:          form.fechaPago,
      tipoComprobanteId:  form.tipoComprobanteId || null,
      noFolio:            form.noFolio || null,
      descripcion:        form.descripcion || null,
      justificacion:      form.justificacion || null,
      gastoIds:           this.data.gastoIds
    };

    this.egresoService.emitirVale(body).subscribe({
      next: () => this.dialogRef.close(1),
      error: () => this.dialogRef.close(2)
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
