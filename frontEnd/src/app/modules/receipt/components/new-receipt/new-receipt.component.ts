import { Component, OnInit, inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { CatalogData, CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { ReceiptService } from 'src/app/modules/shared/services/receipt.service';

import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { UserService } from 'src/app/modules/shared/services/user.service';

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

  conceptos: CatalogOptionModel[] = [];
  usuariosFiltrados: any[] = [];
  usuarioSeleccionado: any;
  years = [2021, 2022, 2023, 2024, 2025, 2026];

  catalogData: CatalogData = {
    cat1: [],
    cat2: [],
    cat3: [],
    cat4: [],
    cat5: [],
    cat6: []
  };

  ngOnInit(): void {
    this.initForm();
    this.getCatalogs();
    this.addMonto();

    this.montoAplicadoArray.valueChanges.subscribe(() => {
      this.updateMontoRecibido();
    });

    this.initUsuarioAutocomplete();
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
    this.saveReceiptData(data);

    const newFolio = Number(this.receiptForm.get('noFolio')?.value) || 0;
    this.receiptForm.get('noFolio')?.setValue(newFolio + 1);
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
