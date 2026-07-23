import { Component, Inject, OnInit, Optional, inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { PersonModel } from 'src/app/modules/shared/models/Person.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { PersonService } from 'src/app/modules/shared/services/person.service';
import { WaterEgresoService } from 'src/app/modules/shared/services/water-egreso.service';
import { ProveedorFormComponent } from '../proveedor-form/proveedor-form.component';

export interface NewEgresoDialogData {
  folioSugerido?: string;
}

@Component({
  selector: 'app-new-egreso',
  templateUrl: './new-egreso.component.html',
  styleUrls: ['./new-egreso.component.css']
})
export class NewEgresoComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly egresoService = inject(WaterEgresoService);
  private readonly catalogService = inject(CatalogService);
  private readonly personService = inject(PersonService);
  private readonly dialog = inject(MatDialog);

  public egresoForm!: FormGroup;
  conceptos: CatalogOptionModel[] = [];
  tiposComprobante: CatalogOptionModel[] = [];

  // "Proveedor" es texto libre (puede ser una empresa como CFE, que no vive
  // en la tabla de personas). "Pagado/gestionado por" es opcional y aparte,
  // para saber quién del comité gestionó/adelantó el pago.
  personas: PersonModel[] = [];
  personasFiltradas: PersonModel[] = [];
  personaSeleccionada: PersonModel | null = null;

  constructor(
    public dialogRef: MatDialogRef<NewEgresoComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: NewEgresoDialogData
  ) { }

  ngOnInit(): void {
    // El folio sugerido (siguiente consecutivo general) se prellena pero
    // queda editable, por si ese vale ya trae su propio folio físico.
    this.egresoForm = this.fb.group({
      fechaPago:        ['', Validators.required],
      fechaCompra:      [''],
      proveedor:        [''],
      personaInput:     [''],
      noFolio:          [this.data?.folioSugerido || ''],
      tipoComprobanteId:[''],
      descripcion:      [''],
      justificacion:    [''],
      lineasArray:      this.fb.array([])
    });

    // Claves de catálogo esperadas (ya existen en Catálogos):
    // CONCEPTOS_EGRESO -> Luz, Nomina, Mantenimiento, Papeleria, Viaticos, Varios, Obras, Cortes
    // TIPO_COMPROBANTE_EGRESO -> Factura, Nota, Vale caja, Recibo nomina, Remisión
    this.catalogService.getOptionsByClave('CONCEPTOS_EGRESO').subscribe({
      next: (opts) => this.conceptos = opts,
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('TIPO_COMPROBANTE_EGRESO').subscribe({
      next: (opts) => this.tiposComprobante = opts,
      error: (e: any) => console.error(e)
    });

    this.personService.getPersons().subscribe({
      next: (resp: any) => {
        if (resp.metadata?.[0]?.code !== '00') return;
        this.personas = resp.data;
      },
      error: (e: any) => console.error(e)
    });

    this.egresoForm.get('personaInput')?.valueChanges
      .pipe(debounceTime(200), distinctUntilChanged())
      .subscribe(value => {
        if (!value || typeof value !== 'string') {
          this.personasFiltradas = [];
          return;
        }
        this.personaSeleccionada = null;
        const texto = value.toLowerCase();
        this.personasFiltradas = this.personas.filter(p =>
          this.nombreCompleto(p).toLowerCase().includes(texto)
        );
      });

    this.addLinea();
  }

  nombreCompleto(p: PersonModel): string {
    return [p.nombre, p.nombre2, p.app, p.apm].filter(Boolean).join(' ');
  }

  displayPersona = (p: PersonModel): string => p ? this.nombreCompleto(p) : '';

  onPersonaSelected(p: PersonModel): void {
    this.personaSeleccionada = p;
  }

  openAgregarPersona(): void {
    const dialogRef = this.dialog.open(ProveedorFormComponent, { width: '600px' });
    dialogRef.afterClosed().subscribe((persona: PersonModel | null) => {
      if (!persona) return;
      this.personas.push(persona);
      this.seleccionarPersona(persona);
    });
  }

  openEditarPersona(): void {
    if (!this.personaSeleccionada) return;
    const dialogRef = this.dialog.open(ProveedorFormComponent, {
      width: '600px',
      data: { persona: this.personaSeleccionada }
    });
    dialogRef.afterClosed().subscribe((persona: PersonModel | null) => {
      if (!persona) return;
      const idx = this.personas.findIndex(p => p.personaId === persona.personaId);
      if (idx >= 0) this.personas[idx] = persona;
      this.seleccionarPersona(persona);
    });
  }

  private seleccionarPersona(persona: PersonModel): void {
    this.personaSeleccionada = persona;
    this.egresoForm.get('personaInput')?.setValue(persona, { emitEvent: false });
  }

  get lineasArray(): FormArray {
    return this.egresoForm.get('lineasArray') as FormArray;
  }

  addLinea(): void {
    this.lineasArray.push(this.fb.group({
      conceptoId:   ['', Validators.required],
      monto:        ['', [Validators.required, Validators.min(0.01)]],
      descripcion:  ['']
    }));
  }

  removeLinea(index: number): void {
    this.lineasArray.removeAt(index);
  }

  get totalAPagar(): number {
    return (this.lineasArray.value as any[])
      .reduce((acc, l) => acc + (Number(l.monto) || 0), 0);
  }

  onSave(): void {
    if (this.egresoForm.invalid || this.lineasArray.length === 0 || this.totalAPagar <= 0) return;

    const form = this.egresoForm.value;
    const data = {
      fechaPago:          form.fechaPago,
      fechaCompra:        form.fechaCompra || null,
      proveedor:          form.proveedor || null,
      personaId:          this.personaSeleccionada?.personaId || null,
      noFolio:            form.noFolio || null,
      tipoComprobanteId:  form.tipoComprobanteId || null,
      descripcion:        form.descripcion || null,
      justificacion:      form.justificacion || null,
      lineas: (form.lineasArray as any[]).map(l => ({
        conceptoId:   l.conceptoId,
        monto:        l.monto,
        descripcion:  l.descripcion || null
      }))
    };

    this.egresoService.save(data).subscribe({
      next: () => this.dialogRef.close(1),
      error: () => this.dialogRef.close(2)
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
