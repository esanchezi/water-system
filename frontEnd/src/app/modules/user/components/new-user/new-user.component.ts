import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { FeeModel } from 'src/app/modules/shared/models/Fee.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { FeeService } from 'src/app/modules/shared/services/fee.service';
import { UserService } from 'src/app/modules/shared/services/user.service';

@Component({
  selector: 'app-new-user',
  templateUrl: './new-user.component.html',
  styleUrls: ['./new-user.component.css']
})
export class NewUserComponent implements OnInit {

  public userForm!: FormGroup;
  private readonly dialogRef      = inject(MatDialogRef);
  private readonly fb             = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);
  private readonly feeService     = inject(FeeService);
  private readonly userService    = inject(UserService);

  listFee: FeeModel[]           = [];

  // Catálogos por clave — sin IDs hardcodeados
  secciones:     CatalogOptionModel[] = [];
  frecuencias:   CatalogOptionModel[] = [];
  estatusPago:   CatalogOptionModel[] = [];

  private readonly anioActual = new Date().getFullYear();
  private readonly ID_FRECUENCIA_DEFAULT = '54';
  private readonly ID_SECCION_DEFAULT   = '1';

  ngOnInit(): void {
    this.initForm();
    this.loadCatalogs();
    this.getAmounts();
  }

  private initForm(): void {
    this.userForm = this.fb.group({
      fkIdCuota:          [null, Validators.required],
      fkFrecuenciaPagoId: [this.ID_FRECUENCIA_DEFAULT, Validators.required],
      estatusPagoId:      [''],
      noUsuario:          ['', Validators.required],
      habitaDomicilio:    ['true', Validators.required],
      tieneToma:          ['true', Validators.required],
      observaciones:      [''],
      email:              [''],
      nombre:             ['', Validators.required],
      nombre2:            ['', Validators.required],
      app:                ['', Validators.required],
      apm:                ['', Validators.required],
      fkIdSeccion:        [this.ID_SECCION_DEFAULT, Validators.required],
      calle:              ['', Validators.required],
      numero:             ['', Validators.required],
      referencia:         [''],
      entreCalle1:        [''],
    });
  }

  private loadCatalogs(): void {
    this.catalogService.getOptionsByClave('SECCIONES_COLONIA').subscribe({
      next: (opts) => this.secciones = opts,
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('FRECUENCIA_PAGO').subscribe({
      next: (opts) => this.frecuencias = opts,
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('ESTATUS_PAGO').subscribe({
      next: (opts) => this.estatusPago = opts,
      error: (e: any) => console.error(e)
    });
  }

  // Se selecciona la categoría de cuota (uso + tipo de usuario), no un monto de
  // un año en particular — el monto vigente se resuelve por año en cuota_monto.
  private getAmounts(): void {
    this.feeService.getFeeAmount().subscribe({
      next: (v: any) => { this.listFee = v.data; },
      error: (e: any) => console.error(e)
    });
  }

  // Monto de referencia para el año actual (o el más reciente disponible si no
  // hay uno registrado todavía para este año).
  montoVigente(fee: FeeModel): number | null {
    if (!fee.amount?.length) return null;
    const exacto = fee.amount.find(a => a.vigencia === this.anioActual);
    if (exacto) return exacto.cuota;
    return [...fee.amount].sort((a, b) => b.vigencia - a.vigencia)[0]?.cuota ?? null;
  }

  onSave(): void {
    if (this.userForm.invalid) return;
    const data = this.prepareUserData();
    this.saveUserData(data);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  private prepareUserData(): any {
    const f = this.userForm.value;
    return {
      cuotaId:         f.fkIdCuota,
      frecuenciaPagoId: f.fkFrecuenciaPagoId,
      estatusPagoId:   f.estatusPagoId,
      noUsuario:       f.noUsuario,
      habitaDomicilio: f.habitaDomicilio,
      tieneToma:       f.tieneToma,
      observaciones:   f.observaciones,
      email:           f.email,
      person: {
        nombre:  f.nombre,
        nombre2: f.nombre2,
        app:     f.app,
        apm:     f.apm,
      },
      adress: {
        seccionId:   f.fkIdSeccion,
        calle:       f.calle,
        numero:      f.numero,
        referencia:  f.referencia,
        entreCalle1: f.entreCalle1,
      }
    };
  }

  private saveUserData(data: any): void {
    this.userService.saveUser(data).subscribe({
      next: () => this.dialogRef.close(true),
      error: () => this.dialogRef.close(false)
    });
  }
}
