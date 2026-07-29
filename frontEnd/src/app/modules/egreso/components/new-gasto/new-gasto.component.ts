import { Component, Inject, OnInit, Optional, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { PersonModel } from 'src/app/modules/shared/models/Person.model';
import { WaterEgresoGastoModel } from 'src/app/modules/shared/models/WaterEgresoGasto.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { PersonService } from 'src/app/modules/shared/services/person.service';
import { WaterEgresoService } from 'src/app/modules/shared/services/water-egreso.service';
import { ProveedorFormComponent } from '../proveedor-form/proveedor-form.component';

export interface NewGastoDialogData {
  gasto?: WaterEgresoGastoModel;
}

// Formulario ligero para capturar (o editar) un gasto suelto durante el mes
// (todavía sin vale): fecha, concepto, monto, proveedor y descripción. Este
// gasto queda "pendiente" hasta que se junta con otros en un vale al emitir.
//
// "Proveedor" y "Pagado/gestionado por" son cosas distintas: el proveedor es
// texto libre (puede ser una empresa como CFE, que no vive en la tabla de
// personas); la persona es opcional y sirve para saber quién del comité
// gestionó o adelantó el pago (casi siempre la misma persona).
@Component({
  selector: 'app-new-gasto',
  templateUrl: './new-gasto.component.html',
  styleUrls: ['./new-gasto.component.css']
})
export class NewGastoComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly egresoService = inject(WaterEgresoService);
  private readonly catalogService = inject(CatalogService);
  private readonly personService = inject(PersonService);
  private readonly dialog = inject(MatDialog);

  public gastoForm!: FormGroup;
  conceptos: CatalogOptionModel[] = [];
  tiposComprobante: CatalogOptionModel[] = [];

  personas: PersonModel[] = [];
  personasFiltradas: PersonModel[] = [];
  personaSeleccionada: PersonModel | null = null;

  // Captura rápida: al guardar un gasto nuevo, el diálogo NO se cierra —
  // solo se limpia el monto, para seguir capturando varios conceptos
  // seguidos (misma fecha/categoría/proveedor) sin reabrir el formulario.
  guardados = 0;
  ultimoGuardadoMonto: number | null = null;
  errorGuardando = false;

  get esEdicion(): boolean {
    return !!this.data?.gasto?.aguaEgresosId;
  }

  constructor(
    public dialogRef: MatDialogRef<NewGastoComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: NewGastoDialogData
  ) { }

  ngOnInit(): void {
    const gasto = this.data?.gasto;

    this.gastoForm = this.fb.group({
      fechaPago:          [gasto?.fechaPago || '', Validators.required],
      conceptoId:         [gasto?.conceptoId || '', Validators.required],
      monto:              [gasto?.monto || '', [Validators.required, Validators.min(0.01)]],
      proveedor:          [gasto?.proveedor || ''],
      personaInput:       [''],
      descripcion:        [gasto?.descripcion || ''],
      tipoComprobanteId:  [gasto?.tipoComprobanteId || '']
    });

    this.catalogService.getOptionsByClave('CONCEPTOS_EGRESO').subscribe({
      next: (opts) => this.conceptos = opts,
      error: (e: any) => console.error(e)
    });

    // Factura/Nota/Remisión (ya tiene su propio comprobante) o Vale caja (se
    // deja vacío aquí y se clasifica hasta que se agrupa en el vale mensual).
    this.catalogService.getOptionsByClave('TIPO_COMPROBANTE_EGRESO').subscribe({
      next: (opts) => this.tiposComprobante = opts,
      error: (e: any) => console.error(e)
    });

    this.personService.getPersons().subscribe({
      next: (resp: any) => {
        if (resp.metadata?.[0]?.code !== '00') return;
        this.personas = resp.data;

        if (gasto?.personaId) {
          const persona = this.personas.find(p => p.personaId === gasto.personaId);
          if (persona) this.seleccionarPersona(persona);
        }
      },
      error: (e: any) => console.error(e)
    });

    this.gastoForm.get('personaInput')?.valueChanges
      .pipe(debounceTime(200), distinctUntilChanged())
      .subscribe(value => {
        if (!value || typeof value !== 'string') {
          this.personasFiltradas = [];
          return;
        }
        // Escribió algo nuevo: la selección previa (si había) ya no aplica,
        // hasta que vuelva a elegir una opción del autocompletable.
        this.personaSeleccionada = null;
        const texto = value.toLowerCase();
        this.personasFiltradas = this.personas.filter(p =>
          this.nombreCompleto(p).toLowerCase().includes(texto)
        );
      });
  }

  nombreCompleto(p: PersonModel): string {
    return [p.nombre, p.nombre2, p.app, p.apm].filter(Boolean).join(' ');
  }

  displayPersona = (p: PersonModel): string => p ? this.nombreCompleto(p) : '';

  onPersonaSelected(p: PersonModel): void {
    this.personaSeleccionada = p;
  }

  // Alta rápida de una persona que todavía no está en Personas, sin salir
  // de este diálogo.
  openAgregarPersona(): void {
    const dialogRef = this.dialog.open(ProveedorFormComponent, { width: '600px' });
    dialogRef.afterClosed().subscribe((persona: PersonModel | null) => {
      if (!persona) return;
      this.personas.push(persona);
      this.seleccionarPersona(persona);
    });
  }

  // Corrige el nombre de la persona ya seleccionada (por ejemplo, si se
  // capturó mal o cambió de apellido), sin salir de este diálogo.
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
    this.gastoForm.get('personaInput')?.setValue(persona, { emitEvent: false });
  }

  onSave(): void {
    if (this.gastoForm.invalid) return;

    const form = this.gastoForm.value;
    const data = {
      fechaPago:          form.fechaPago,
      conceptoId:         form.conceptoId,
      monto:              form.monto,
      proveedor:          form.proveedor || null,
      personaId:          this.personaSeleccionada?.personaId || null,
      descripcion:        form.descripcion || null,
      tipoComprobanteId:  form.tipoComprobanteId || null
    };

    this.errorGuardando = false;

    if (this.esEdicion) {
      // Editar un gasto existente es una acción puntual: se cierra al guardar.
      this.egresoService.actualizarGasto(this.data.gasto!.aguaEgresosId!, data).subscribe({
        next: () => this.dialogRef.close(1),
        error: () => this.dialogRef.close(2)
      });
      return;
    }

    // Captura nueva: no se cierra el diálogo. Solo se limpia el monto (y la
    // descripción, que suele ser distinta por cada recibo) para seguir
    // capturando rápido; fecha, categoría y proveedor se quedan igual.
    this.egresoService.crearGasto(data).subscribe({
      next: () => {
        this.guardados++;
        this.ultimoGuardadoMonto = form.monto;
        this.gastoForm.get('monto')?.reset('');
        this.gastoForm.get('descripcion')?.reset('');
      },
      error: () => this.errorGuardando = true
    });
  }

  onCerrar(): void {
    this.dialogRef.close(this.guardados > 0 ? 1 : undefined);
  }
}
