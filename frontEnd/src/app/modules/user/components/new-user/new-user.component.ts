import { Component, Inject, OnInit, Optional, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { FeeModel } from 'src/app/modules/shared/models/Fee.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { FeeService } from 'src/app/modules/shared/services/fee.service';
import { UserService } from 'src/app/modules/shared/services/user.service';
import { PreregistroUsuarioService } from 'src/app/modules/shared/services/preregistro-usuario.service';

// Cuando se abre "copiando" a otro usuario ya existente (mismo domicilio,
// otro integrante de la familia con su propia toma/cuenta): se prellenan
// datos personales y domicilio, pero N° Usuario, Cuota y Observaciones se
// quedan en blanco porque son justo lo que cambia entre uno y otro.
//
// Cuando se abre desde "convertir a usuario" de un preregistro: se prellena
// el nombre capturado en el preregistro (tal cual, sin separar en
// nombre/apellidos porque ahí solo se guardó como texto libre), y al
// guardar exitosamente se enlaza automáticamente esta casa al usuario nuevo
// y se marca el preregistro como convertido -- todo en un solo paso, sin
// que la usuaria tenga que anotar el N° de usuario a mano después.
export interface NewUserDialogData {
  copyFrom?: any;
  preregistroId?: number;
  casaIdDestino?: number;
  nombrePrellenado?: string;
}

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
  private readonly preregistroService = inject(PreregistroUsuarioService);

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: NewUserDialogData) { }

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
    this.aplicarCopia();
    this.aplicarPrellenadoPreregistro();
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

  get esCopia(): boolean {
    return !!this.data?.copyFrom;
  }

  // Copia datos personales, domicilio y demás datos del usuario "fuente"
  // (viene de details/{usuarioId}) a este formulario -- todo excepto
  // N° Usuario, Cuota y Observaciones, que se dejan en blanco a propósito
  // porque son justo lo que cambia entre uno y otro usuario de la misma casa.
  private aplicarCopia(): void {
    const u = this.data?.copyFrom;
    if (!u) return;

    this.userForm.patchValue({
      fkFrecuenciaPagoId: u.frecuenciaPagoId ?? this.ID_FRECUENCIA_DEFAULT,
      estatusPagoId:      u.estatusPagoId ?? '',
      habitaDomicilio:    String(u.habitaDomicilio ?? true),
      tieneToma:          String(u.tieneToma ?? true),
      email:              u.email ?? '',
      nombre:             u.nombre ?? '',
      nombre2:            u.nombre2 ?? '',
      app:                u.app ?? '',
      apm:                u.apm ?? '',
      fkIdSeccion:        u.seccionId ?? this.ID_SECCION_DEFAULT,
      calle:              u.calle ?? '',
      numero:             u.numero ?? '',
      referencia:         u.referencia ?? '',
      entreCalle1:        u.entrecalle1 ?? '',
    });
  }

  get esConversionPreregistro(): boolean {
    return !!this.data?.preregistroId;
  }

  // El preregistro solo guarda "nombre" como texto libre (no separado en
  // nombre/apellidos), así que aquí solo se prellena el primer campo -- la
  // usuaria completa/corrige el resto al capturar.
  private aplicarPrellenadoPreregistro(): void {
    if (!this.data?.preregistroId) return;
    this.userForm.patchValue({
      nombre: this.data.nombrePrellenado ?? ''
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
      next: (usuarioCreado: any) => {
        // Si viene de "convertir a usuario" de un preregistro: enlazamos la
        // casa y marcamos el preregistro como convertido antes de cerrar,
        // para que quede en un solo paso (la usuaria no tiene que anotar
        // el N° de usuario a mano).
        if (this.esConversionPreregistro && usuarioCreado?.aguaUsuarioId) {
          this.enlazarConversionPreregistro(usuarioCreado.aguaUsuarioId);
        } else {
          this.dialogRef.close(true);
        }
      },
      error: () => this.dialogRef.close(false)
    });
  }

  private enlazarConversionPreregistro(aguaUsuarioId: number): void {
    const casaId = this.data.casaIdDestino;
    const preregistroId = this.data.preregistroId!;

    const marcarConvertido = () => {
      this.preregistroService.marcarConvertido(preregistroId, aguaUsuarioId).subscribe({
        next: () => this.dialogRef.close(true),
        // El usuario ya se creó correctamente aunque falle el enlace del
        // preregistro -- no bloqueamos el cierre por esto, solo se quedaría
        // el preregistro como pendiente para enlazar después.
        error: () => this.dialogRef.close(true)
      });
    };

    if (casaId) {
      this.userService.assignHouse(aguaUsuarioId, casaId).subscribe({
        next: () => marcarConvertido(),
        error: () => marcarConvertido()
      });
    } else {
      marcarConvertido();
    }
  }
}
