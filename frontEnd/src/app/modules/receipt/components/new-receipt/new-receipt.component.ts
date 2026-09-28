import { Component, Inject, OnInit, Optional, inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CatalogData, CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { ReceiptService } from 'src/app/modules/shared/services/receipt.service';

import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { UserService } from 'src/app/modules/shared/services/user.service';
import { WaterUserAnnualPaymentService } from 'src/app/modules/shared/services/water-user-annual-payment.service';
import { AvisoAdeudoService } from 'src/app/modules/shared/services/aviso-adeudo.service';
import { AvisoAdeudoModel } from 'src/app/modules/shared/models/AvisoAdeudo.model';
import Swal from 'sweetalert2';

// Cuando se abre para EDITAR (data.receipt trae el recibo completo, tal cual
// lo devuelve el backend con todas sus líneas de montoAplicado), el
// formulario se prellena con todo y "Guardar" hace un PUT en vez de un POST.
//
// Cuando se abre desde la ficha de un usuario (ej. "Detalle pagos"), ya se
// sabe quién es -- data.usuarioPreseleccionado trae { noUsuario,
// nombreCompleto, aguaUsuarioId } y el campo de usuario se prellena y se
// bloquea, para que solo haga falta capturar el folio y el resto del
// recibo. aguaUsuarioId es opcional (para no romper llamadores viejos) pero
// hace falta para poder consultar sus avisos de adeudo pendientes de cobro.
export interface NewReceiptDialogData {
  receipt?: any;
  usuarioPreseleccionado?: { noUsuario: number; nombreCompleto: string; aguaUsuarioId?: number };
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
  private readonly avisoAdeudoService = inject(AvisoAdeudoService);

  conceptos: CatalogOptionModel[] = [];
  usuariosFiltrados: any[] = [];
  usuarioSeleccionado: any;
  years = [2021, 2022, 2023, 2024, 2025, 2026];

  // Cartas de adeudo ya entregadas al usuario seleccionado que aún no se
  // marcan como atendidas -- se muestran como checklist para poder
  // vincularlas de una vez a este mismo recibo (ver onSave()/
  // vincularAvisosSeleccionados()), sin tener que ir después a la ficha del
  // usuario a marcarlas "atendida" con el folio a mano.
  avisosPendientes: AvisoAdeudoModel[] = [];
  avisosSeleccionados: Set<number> = new Set();

  // Años que el usuario seleccionado YA tiene marcados como pagados (de
  // cualquier recibo anterior) -- se muestran junto al checkbox de cada año
  // para poder validar de un vistazo antes de marcar otro, sin tener que
  // salirse del formulario a revisar el historial del usuario.
  aniosPagadosUsuario: Set<number> = new Set();

  // Detalle del recibo más reciente del usuario seleccionado (folio, fecha,
  // concepto, total) -- solo para consulta visual mientras se captura un
  // recibo nuevo, no se usa para nada del guardado.
  ultimoRecibo: any = null;

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

  // Cuando viene de la ficha de un usuario ya no tiene caso dejar el
  // diálogo abierto para "seguir capturando" (siempre sería el mismo
  // usuario) -- ver onSave().
  get usuarioBloqueado(): boolean {
    return !!this.data?.usuarioPreseleccionado;
  }

  ngOnInit(): void {
    this.initForm();
    this.getCatalogs();

    if (this.data?.receipt) {
      this.cargarRecibo(this.data.receipt);
    } else if (this.data?.usuarioPreseleccionado) {
      this.preseleccionarUsuario(this.data.usuarioPreseleccionado);
      this.addMonto();
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

  // Ya se conoce al usuario (se abrió desde su ficha) -- se prellena el
  // campo y se bloquea para que no se pueda cambiar por accidente, no hace
  // falta usar el buscador.
  private preseleccionarUsuario(usuario: { noUsuario: number; nombreCompleto: string; aguaUsuarioId?: number }): void {
    this.usuarioSeleccionado = usuario;
    this.cargarAniosPagados(usuario?.noUsuario);
    this.cargarAvisosPendientes(usuario?.aguaUsuarioId);
    this.cargarUltimoRecibo(usuario?.noUsuario);
    this.receiptForm.patchValue({ noUsuario: usuario }, { emitEvent: false });
    this.receiptForm.get('noUsuario')?.disable();
  }

  // Avisos de adeudo (Primer/Segundo) ya entregados a este usuario que
  // siguen sin marcarse como atendidos -- normalmente porque aún no se
  // capturaba el recibo que los liquida, que es justo lo que se está
  // haciendo ahora mismo en este formulario.
  private cargarAvisosPendientes(aguaUsuarioId?: number): void {
    this.avisosPendientes = [];
    this.avisosSeleccionados = new Set();
    if (!aguaUsuarioId) return;

    this.avisoAdeudoService.getPendientesDeAtencion(aguaUsuarioId).subscribe({
      next: (resp: any) => {
        // metadata viene como objeto plano ({code, message, detail}), no
        // como arreglo -- ver BaseRestResponse.java. AvisoAdeudoRestResponse
        // sigue ese mismo patrón (a diferencia de otros servicios de este
        // módulo, como WaterUserAnnualPaymentService, cuyo backend sí
        // envuelve metadata en un arreglo).
        if (resp.metadata?.code !== '00') return;
        this.avisosPendientes = resp.data || [];
      },
      error: (e: any) => console.error(e)
    });
  }

  toggleAviso(avisoAdeudoId: number): void {
    if (this.avisosSeleccionados.has(avisoAdeudoId)) {
      this.avisosSeleccionados.delete(avisoAdeudoId);
    } else {
      this.avisosSeleccionados.add(avisoAdeudoId);
    }
  }

  // Tras guardar el recibo exitosamente, marca como atendido (PAGADO) cada
  // aviso que se haya marcado en el checklist, vinculando el folio recién
  // capturado -- reusa el mismo endpoint que ya usa la ficha de usuario
  // para marcar atendida una carta, no se duplica lógica de validación.
  private vincularAvisosSeleccionados(folioParaVincular?: number, avisosParaVincular?: Set<number>): void {
    const folio = folioParaVincular ?? (Number(this.receiptForm.get('noFolio')?.value) || undefined);
    const avisos = avisosParaVincular ?? this.avisosSeleccionados;
    if (!avisos || avisos.size === 0) return;

    avisos.forEach(avisoAdeudoId => {
      this.avisoAdeudoService.marcarAtendida(avisoAdeudoId, {
        resultadoAtencion: 'PAGADO',
        folioReciboVinculado: folio
      }).subscribe({
        error: (e: any) => console.error('No se pudo vincular el aviso ' + avisoAdeudoId, e)
      });
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
    // user viene del autocomplete (AguaUsuarioSearchDTO), que ya trae
    // aguaUsuarioId -- no hace falta una consulta aparte para tenerlo.
    this.cargarAvisosPendientes(user?.aguaUsuarioId);
    this.cargarUltimoRecibo(user?.noUsuario);
  }

  // Último recibo capturado a este usuario (folio, fecha, concepto, total)
  // -- solo para consultarlo de un vistazo mientras se captura uno nuevo, ej.
  // para comparar montos o evitar capturar el mismo concepto dos veces.
  // Reusa findByNoUsuario, que ya viene ordenado más reciente primero.
  private cargarUltimoRecibo(noUsuario: any): void {
    this.ultimoRecibo = null;
    if (!noUsuario) return;

    this.receiptService.getReceiptByNoUser(noUsuario).subscribe({
      next: (resp: any) => {
        // WaterReceiptRestResponse extiende RestResponse (no BaseRestResponse)
        // -- aquí metadata sí es un arreglo, como en WaterUserAnnualPaymentService.
        if (resp.metadata?.[0]?.code !== '00') return;
        this.ultimoRecibo = (resp.data || [])[0] || null;
      },
      // 404 = el usuario aún no tiene recibos -- no es un error real, solo
      // significa que no hay nada que mostrar.
      error: () => { this.ultimoRecibo = null; }
    });
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
    // Al editar no aplica alertar por folio duplicado: es el mismo recibo
    // que ya existe con ese folio, no uno nuevo.
    if (this.esEdicion) {
      this.guardarRecibo();
      return;
    }

    const folio = Number(this.receiptForm.get('noFolio')?.value) || 0;
    this.verificarFolioDuplicado(folio, () => this.guardarRecibo());
  }

  // Si el folio que se está por guardar ya existe en otro recibo, avisa
  // antes de continuar -- pedido explícito de Ely para no duplicar folios
  // por error. No bloquea el guardado (puede haber casos legítimos, ej.
  // folios repetidos entre comités distintos), solo pide confirmar.
  private verificarFolioDuplicado(folio: number, continuar: () => void): void {
    if (!folio) { continuar(); return; }

    this.receiptService.getReceiptByFolioExacto(folio).subscribe({
      next: (resp: any) => {
        if (resp.metadata?.[0]?.code !== '00') { continuar(); return; }
        const existente = (resp.data || [])[0];
        if (!existente) { continuar(); return; }

        const persona = existente.waterUser?.person;
        const nombre = [persona?.nombre, persona?.nombre2, persona?.app, persona?.apm].filter(Boolean).join(' ');
        Swal.fire({
          icon: 'warning',
          title: `El folio ${folio} ya fue capturado`,
          html: `Corresponde a ${existente.waterUser?.noUsuario ?? '--'}`
            + (nombre ? ` - ${nombre}` : '')
            + `, fecha ${existente.fecha ? String(existente.fecha).slice(0, 10) : '--'}, `
            + `total $${existente.total ?? '--'}.<br><br>¿Deseas guardarlo de todas formas?`,
          showCancelButton: true,
          confirmButtonText: 'Guardar de todas formas',
          cancelButtonText: 'Cancelar',
          confirmButtonColor: '#d33'
        }).then(result => {
          if (result.isConfirmed) continuar();
        });
      },
      // 404 = no existe ningún recibo con ese folio todavía -- es el caso
      // normal, no bloquea el guardado. Un error de red tampoco debe
      // impedir capturar el recibo.
      error: () => continuar()
    });
  }

  private guardarRecibo(): void {
    const data = this.prepareUserData();

    // Editar un recibo ya existente es una acción puntual: se guarda y se
    // cierra el diálogo (a diferencia de capturar uno nuevo, que se queda
    // abierto para seguir con el siguiente).
    if (this.esEdicion) {
      this.receiptService.updateReceipt(this.editandoReciboId!, data).subscribe({
        next: () => {
          this.vincularAvisosSeleccionados();
          this.dialogRef.close(1);
        },
        error: () => this.dialogRef.close(2)
      });
      return;
    }

    if (this.usuarioBloqueado) {
      // Un recibo a la vez para este usuario -- se guarda y se cierra,
      // a diferencia del flujo general de "Pagos" que se queda abierto
      // para capturar varios recibos de usuarios distintos seguidos.
      this.receiptService.saveReceipt(data).subscribe({
        next: () => {
          this.vincularAvisosSeleccionados();
          this.dialogRef.close(1);
        },
        error: () => this.dialogRef.close(2)
      });
      return;
    }

    // Se toma una foto de qué avisos quedaron marcados ANTES de limpiar el
    // formulario para el siguiente recibo (más abajo) -- si se leyera
    // this.avisosSeleccionados directo dentro del next() de guardado, para
    // cuando la respuesta llegue ya se habría vaciado.
    this.saveReceiptData(data, Number(this.receiptForm.get('noFolio')?.value) || undefined, new Set(this.avisosSeleccionados));

    const newFolio = Number(this.receiptForm.get('noFolio')?.value) || 0;
    this.receiptForm.get('noFolio')?.setValue(newFolio + 1);

    // Limpia el usuario para el siguiente recibo -- folio, fecha, comité,
    // tipo de pago, etc. se quedan igual porque casi siempre se repiten,
    // pero el usuario SIEMPRE cambia de un recibo a otro.
    this.receiptForm.get('noUsuario')?.setValue('');
    this.usuarioSeleccionado = null;
    this.avisosPendientes = [];
    this.avisosSeleccionados = new Set();
    this.ultimoRecibo = null;
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

  private saveReceiptData(data: any, folioParaVincular?: number, avisosParaVincular?: Set<number>): void {
    this.receiptService.saveReceipt(data)
      .subscribe({
        next: () => {
          console.info('Guardado exitoso');
          this.vincularAvisosSeleccionados(folioParaVincular, avisosParaVincular);
        },
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
