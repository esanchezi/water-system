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

  // Si falla el guardado (ej. validación del backend), el diálogo se queda
  // abierto con todo lo capturado -- no tiene caso perder todo el formulario
  // por un error que se puede corregir ahí mismo.
  guardando = false;
  errorGuardando = false;
  mensajeError = '';

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
      conceptoId:       [''],
      // Solo aplica cuando el vale es "simple" (sin líneas): el monto se
      // captura directo aquí. Si se agrega al menos una línea, el monto se
      // calcula solo (suma de líneas) y este campo deja de usarse.
      monto:            [''],
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

    // Un vale puede ser "simple" (una sola cosa, sin líneas -- ej. un pago
    // único con su propia factura) o desglosado en varias líneas. Empieza
    // como simple; en cuanto se agrega la primera línea con "Agregar gasto"
    // pasa al modo desglosado.
    this.actualizarValidadoresModoSimple();
  }

  // Modo simple = sin líneas: el vale usa directamente monto + categoría de
  // la cabecera. En ese modo, monto y categoría son obligatorios (no hay
  // líneas de las que tomarlos). En modo desglosado son opcionales/no
  // aplican (el monto se calcula solo, la categoría es opcional).
  get esModoSimple(): boolean {
    return this.lineasArray.length === 0;
  }

  private actualizarValidadoresModoSimple(): void {
    const montoCtrl = this.egresoForm.get('monto');
    const conceptoCtrl = this.egresoForm.get('conceptoId');
    if (this.esModoSimple) {
      montoCtrl?.setValidators([Validators.required, Validators.min(0.01)]);
      conceptoCtrl?.setValidators([Validators.required]);
    } else {
      montoCtrl?.clearValidators();
      conceptoCtrl?.clearValidators();
    }
    montoCtrl?.updateValueAndValidity();
    conceptoCtrl?.updateValueAndValidity();
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

  // Cada línea es, en esencia, un gasto (mismos campos que "Gastos del
  // mes"): proveedor/fecha/folio son opcionales -- si se dejan vacíos, la
  // línea toma visualmente los del vale completo (caso normal: un solo
  // proveedor, un solo pago). Solo se llenan cuando ese gasto en particular
  // fue con otro proveedor, otra fecha, o trae su propio comprobante.
  //
  // Al agregar una línea nueva se prellena con categoría/proveedor/fecha de
  // la línea anterior (lo más común es que varias líneas seguidas sean de la
  // misma categoría y proveedor, solo cambiando el monto/fecha) -- así no
  // hay que volver a elegirlos cada vez. "Limpiar" (limpiarLinea) sirve para
  // cuando sí cambia y prefieres partir de cero en esa línea.
  addLinea(): void {
    const anterior = this.lineasArray.length > 0
      ? this.lineasArray.at(this.lineasArray.length - 1).value
      : null;

    this.lineasArray.push(this.fb.group({
      conceptoId:   [anterior?.conceptoId || '', Validators.required],
      monto:        ['', [Validators.required, Validators.min(0.01)]],
      descripcion:  [''],
      proveedor:    [anterior?.proveedor || ''],
      fechaPago:    [anterior?.fechaPago || ''],
      noFolio:      ['']
    }));
    this.actualizarValidadoresModoSimple();
  }

  limpiarLinea(index: number): void {
    this.lineasArray.at(index).reset({
      conceptoId: '', monto: '', descripcion: '', proveedor: '', fechaPago: '', noFolio: ''
    });
  }

  removeLinea(index: number): void {
    this.lineasArray.removeAt(index);
    this.actualizarValidadoresModoSimple();
  }

  get totalAPagar(): number {
    if (this.esModoSimple) {
      return Number(this.egresoForm.get('monto')?.value) || 0;
    }
    return (this.lineasArray.value as any[])
      .reduce((acc, l) => acc + (Number(l.monto) || 0), 0);
  }

  onSave(): void {
    if (this.egresoForm.invalid || this.totalAPagar <= 0) return;

    const form = this.egresoForm.value;
    const data = {
      fechaPago:          form.fechaPago,
      fechaCompra:        form.fechaCompra || null,
      proveedor:          form.proveedor || null,
      personaId:          this.personaSeleccionada?.personaId || null,
      noFolio:            form.noFolio || null,
      tipoComprobanteId:  form.tipoComprobanteId || null,
      conceptoId:         form.conceptoId || null,
      // Solo se manda cuando el vale es simple (sin líneas); si hay líneas,
      // el backend calcula el monto solo y este valor se ignora.
      monto:              this.esModoSimple ? form.monto : null,
      descripcion:        form.descripcion || null,
      justificacion:      form.justificacion || null,
      lineas: (form.lineasArray as any[]).map(l => ({
        conceptoId:   l.conceptoId,
        monto:        l.monto,
        descripcion:  l.descripcion || null,
        proveedor:    l.proveedor || null,
        fechaPago:    l.fechaPago || null,
        noFolio:      l.noFolio || null
      }))
    };

    this.guardando = true;
    this.errorGuardando = false;

    this.egresoService.save(data).subscribe({
      next: () => this.dialogRef.close(1),
      error: (e: any) => {
        this.guardando = false;
        this.errorGuardando = true;
        this.mensajeError = e?.error?.metadata?.[0]?.date || 'Error al guardar el vale, intenta de nuevo.';
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
