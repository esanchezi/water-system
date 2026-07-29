import { Component, Inject, OnInit, Optional, inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CatalogData, CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { ReceiptService } from 'src/app/modules/shared/services/receipt.service';

import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { UserService } from 'src/app/modules/shared/services/user.service';
import { WaterUserAnnualPaymentService } from 'src/app/modules/shared/services/water-user-annual-payment.service';

// Cuando se abre para EDITAR (data.receipt trae el recibo completo, tal cual
// lo devuelve el backend con todas sus líneas de montoAplicado), el
// formulario se prellena con todo y "Guardar" hace un PUT en vez de un POST.
export interface NewReceiptDialogData {
  receipt?: any;
}

@Component({
  selector: 'app-new-receipt',
  templateUrl: './new-receipt.component.html',
  styleUrls: ['./new-receipt.component.css']
})
export class NewReceiptComponent implements OnInit {

  public receiptForm!: FormGroup;

  private readonly dialogRef = inject(MatDialogRef);
  private readonly fb = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);
  private readonly receiptService = inject(ReceiptService);
  private readonly usuarioService = inject(UserService);
  private readonly annualPaymentService = inject(WaterUserAnnualPaymentService);

  conceptos: CatalogOptionModel[] = [];
  usuariosFiltrados: any[] = [];
  usuarioSeleccionado: any;
  years = [2021, 2022, 2023, 2024, 2025, 2026];

  // Años que el usuario seleccionado YA tiene marcados como pagados (de
  // cualquier recibo anterior) -- se muestran junto al checkbox de cada año
  // para poder validar de un vistazo antes de marcar otro, sin tener que
  // salirse del formulario a revisar el historial del usuario.
  aniosPagadosUsuario: Set<number> = new Set();

  // Si trae valor, "Guardar" edita ese recibo (PUT) en vez de crear uno
  // nuevo; también cambia el comportamiento de cierre del diálogo (editar
  // sí cierra al terminar, capturar uno nuevo se queda abierto para seguir).
  editandoReciboId: number | null = null;

  catalogData: CatalogData = {
    cat1: [],
    cat2: [],
    cat3: [],
    cat4: [],
    cat5: [],
    cat6: []
  };

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: NewReceiptDialogData) { }

  get esEdicion(): boolean {
    return this.editandoReciboId != null;
  }

  ngOnInit(): void {
    this.initForm();
    this.getCatalogs();

    if (this.data?.receipt) {
      this.cargarRecibo(this.data.receipt);
    } else {
      this.addMonto();
    }

    this.montoAplicadoArray.valueChanges.subscribe(() => {
      this.updateMontoRecibido();
    });

    this.initUsuarioAutocomplete();
  }

  // Prellena el formulario completo (incluyendo todas las líneas de monto
  // aplicado) a partir de un recibo ya guardado, para poder modificarlo.
  private cargarRecibo(receipt: any): void {
    this.editandoReciboId = receipt.aguaReciboId;

    const primerPago = receipt.waterReceiptPayment?.[0];
    const persona = receipt.waterUser?.person;
    const nombreCompleto = [persona?.nombre, persona?.nombre2, persona?.app, persona?.apm]
      .filter(Boolean).join(' ');
    this.usuarioSeleccionado = receipt.waterUser
      ? { noUsuario: receipt.waterUser.noUsuario, nombreCompleto }
      : null;
    this.cargarAniosPagados(receipt.waterUser?.noUsuario);

    this.receiptForm.patchValue({
      noUsuario: this.usuarioSeleccionado,
      noFolio: receipt.noFolio,
      fecha: receipt.fecha,
      observaciones: receipt.observaciones,
      concepto: receipt.concepto,
      comiteId: primerPago?.comiteId ?? '',
      tipoPagoId: primerPago?.tipoPagoId ?? '',
      fechaPago: primerPago?.fechaPago ? String(primerPago.fechaPago).slice(0, 16) : '',
      montoRecibido: primerPago?.montoRecibido ?? receipt.total ?? 0,
      estatusComite: receipt.waterUser?.estatusComiteId ?? '',
      estatusPago: receipt.waterUser?.estatusPagoId ?? '',
    }, { emitEvent: false });

    const pagos = receipt.waterReceiptPayment?.length ? receipt.waterReceiptPayment : [{}];
    pagos.forEach((pago: any) => {
      this.montoAplicadoArray.push(this.fb.group({
        montoAplicado: [pago.montoAplicado ?? 0, Validators.required],
        conceptoIdM: [pago.conceptoId ?? '', Validators.required],
        anio: [pago.anio ?? '', Validators.required]
      }));
    });
  }

  private initUsuarioAutocomplete(): void {
    this.receiptForm.get('noUsuario')?.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(value => {

        if (!value || typeof value !== 'string') return;

        this.usuarioService.searchUsersByNumber(value)
          .subscribe((resp: any) => {
            this.usuariosFiltrados = resp;
          });
      });
  }

  displayUsuario(user: any): string {
    return user ? `${user.noUsuario} - ${user.nombreCompleto}` : '';
  }

  onUsuarioSelected(user: any): void {
    this.usuarioSeleccionado = user;
    this.cargarAniosPagados(user?.noUsuario);
  }

  // Consulta qué años ya tiene pagados el usuario seleccionado, para
  // mostrarlo junto al checkbox de "Años liquidados con este recibo" y
  // poder validar de un vistazo (evita marcar dos veces el mismo año o
  // saltarse uno sin darte cuenta).
  private cargarAniosPagados(noUsuario: any): void {
    this.aniosPagadosUsuario = new Set();
    if (!noUsuario) return;

    this.annualPaymentService.getByNoUser(noUsuario).subscribe({
      next: (resp: any) => {
        if (resp.metadata?.[0]?.code !== '00') return;
        this.aniosPagadosUsuario = new Set((resp.data || []).map((p: any) => p.anio));
      },
      error: (e: any) => console.error(e)
    });
  }

  displayUser(user: any): string {
    return user ? `${user.noUsuario} - ${user.nombreCompleto}` : '';
  }

  onChange(event: Event) {
    const newValue = (event.target as HTMLInputElement).value;
    if (!newValue) return;
    const fechaFormateada = newValue + " 00:00";
    this.receiptForm.get('fechaPago')?.setValue(fechaFormateada);
  }

  private initForm(): void {
    this.receiptForm = this.fb.group({
      noUsuario: ['', Validators.required],
      conceptoId: ['', Validators.required],
      noFolio: ['', Validators.required],
      fecha: ['', Validators.required],
      observaciones: ['', Validators.required],
      concepto: ['', Validators.required],
      comiteId: ['', Validators.required],
      tipoPagoId: [''],
      fechaPago: [''],
      montoRecibido: [0, Validators.required],
      montoAplicado: ['', Validators.required],
      anio: ['', Validators.required],
      estatusComite: ['', Validators.required],
      estatusPago: ['', Validators.required],
      conceptoIdM: ['', Validators.required],
      montoAplicadoArray: this.fb.array([]),
      aniosPagadosArray: this.fb.array(
        this.years.map(() => this.fb.control(false))
      ),
    });
  }

  get montoAplicadoArray(): FormArray {
    return this.receiptForm.get('montoAplicadoArray') as FormArray;
  }

  get aniosPagadosArray(): FormArray {
    return this.receiptForm.get('aniosPagadosArray') as FormArray;
  }
  addMonto(): void {
    const montoGroup = this.fb.group({
      montoAplicado: [0, Validators.required],
      conceptoIdM: ['', Validators.required],
      anio: ['', Validators.required]
    });
    this.montoAplicadoArray.push(montoGroup);
  }

  removeMonto(index: number): void {
    this.montoAplicadoArray.removeAt(index);
    this.updateMontoRecibido();
  }

  onSave(): void {
    const data = this.prepareUserData();

    // Editar un recibo ya existente es una acción puntual: se guarda y se
    // cierra el diálogo (a diferencia de capturar uno nuevo, que se queda
    // abierto para seguir con el siguiente).
    if (this.esEdicion) {
      this.receiptService.updateReceipt(this.editandoReciboId!, data).subscribe({
        next: () => this.dialogRef.close(1),
        error: () => this.dialogRef.close(2)
      });
      return;
    }

    this.saveReceiptData(data);

    const newFolio = Number(this.receiptForm.get('noFolio')?.value) || 0;
    this.receiptForm.get('noFolio')?.setValue(newFolio + 1);

    // Limpia el usuario para el siguiente recibo -- folio, fecha, comité,
    // tipo de pago, etc. se quedan igual porque casi siempre se repiten,
    // pero el usuario SIEMPRE cambia de un recibo a otro.
    this.receiptForm.get('noUsuario')?.setValue('');
    this.usuarioSeleccionado = null;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  private getCatalogs(): void {
    this.getOptionsById(2, 'cat1', 'concepto');
    this.getOptionsById(3, 'cat2', 'comite');
    this.getOptionsById(4, 'cat3', 'Tipo Pago');
    this.getOptionsById(12, 'cat4', 'estatusComite');
    this.getOptionsById(13, 'cat5', 'estatusPago');
  }

  private getOptionsById(id: any, property: keyof CatalogData, catalogType: string): void {
    this.catalogService.getCatalogById(id)
      .subscribe({
        next: (v: any) => {
          this.catalogData[property] = v.data[0].options;
        },
        error: (e) => console.error(e),
        complete: () => console.info(`Obtención de ${catalogType} completada`)
      });
  }

  private prepareUserData(): any {

    let waterReceiptPayments =
      this.receiptForm.get('montoAplicadoArray')?.value.map((monto: any) => ({
        comiteId: this.receiptForm.get('comiteId')?.value,
        tipoPagoId: this.receiptForm.get('tipoPagoId')?.value,
        conceptoId: monto.conceptoIdM,
        fechaPagoStr: this.formatearFechaParaBackend(this.receiptForm.get('fechaPago')?.value),
        montoRecibido: this.receiptForm.get('montoRecibido')?.value,
        montoAplicado: monto.montoAplicado,
        anio: monto.anio,
      }));

    const aniosPagados = this.obtenerAniosMarcados();

    return {
      conceptoId: 6,
      noFolio: this.receiptForm.get('noFolio')?.value,
      fechaStr: this.receiptForm.get('fecha')?.value,
      observaciones: this.receiptForm.get('observaciones')?.value,
      concepto: this.receiptForm.get('concepto')?.value,
      total: this.receiptForm.get('montoRecibido')?.value,
      aniosPagados: aniosPagados,
      waterReceiptPayment: waterReceiptPayments,
      waterUser: {
        noUsuario: this.usuarioSeleccionado?.noUsuario,
        estatusComiteId: this.receiptForm.get('estatusComite')?.value,
        estatusPagoId: this.receiptForm.get('estatusPago')?.value,
      }
    };
  }

  private saveReceiptData(data: any): void {
    this.receiptService.saveReceipt(data)
      .subscribe({
        next: () => console.info('Guardado exitoso'),
        error: () => console.error('Error al guardar'),
        complete: () => console.info('Proceso completado')
      });
  }

  updateMontoRecibido(): void {
    const total = this.montoAplicadoArray.controls.reduce((acc, control) => {
      const value = Number(control.get('montoAplicado')?.value) || 0;
      return acc + value;
    }, 0);

    this.receiptForm.get('montoRecibido')?.setValue(total, { emitEvent: false });
  }

  private formatearFechaParaBackend(fecha: string): string {

    if (!fecha) return fecha;
    if (fecha.includes('T')) {
      return fecha.replace('T', ' ');
    }
    return fecha;
  }

  private obtenerAniosMarcados(): number[] {
    return this.aniosPagadosArray.controls
      .map((control, index) =>
        control.value ? this.years[index] : null
      )
      .filter((value): value is number => value !== null);
  }
}
