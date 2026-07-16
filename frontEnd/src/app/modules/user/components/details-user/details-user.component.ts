import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { WaterAgreementModel } from 'src/app/modules/shared/models/WaterAgreement.model';
import { AgreementService } from 'src/app/modules/shared/services/agreement.service';
import { NewConvenioComponent } from 'src/app/modules/convenio/components/new-convenio/new-convenio.component';
import { AssemblyModel } from 'src/app/modules/shared/models/Assembly.model';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { FeeAmountModel, FeeModel } from 'src/app/modules/shared/models/Fee.model';
import { PersonModel } from 'src/app/modules/shared/models/Person.model';
import { WaterReceiptModel } from 'src/app/modules/shared/models/WaterReceipt.model';
import { WaterHouseModel, WaterUserDetailModel, WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';
import { WaterUserNotifyModel } from 'src/app/modules/shared/models/WaterUserNotify.model';
import { WaterUserChargeModel } from 'src/app/modules/shared/models/WaterUserCharge.model';
import { AssemblyService } from 'src/app/modules/shared/services/assembly.service';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { FeeService } from 'src/app/modules/shared/services/fee.service';
import { PersonService } from 'src/app/modules/shared/services/person.service';
import { ReceiptService } from 'src/app/modules/shared/services/receipt.service';
import { UserNoticeService } from 'src/app/modules/shared/services/user.notice.service';
import { UserChargeService } from 'src/app/modules/shared/services/user-charge.service';
import { UserService } from 'src/app/modules/shared/services/user.service';
import { HouseService } from 'src/app/modules/shared/services/house.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-details-user',
  templateUrl: './details-user.component.html',
  styleUrls: ['./details-user.component.css']
})
export class DetailsUserComponent implements OnInit {

  private readonly fb               = inject(FormBuilder);
  private readonly activatedRoute   = inject(ActivatedRoute);
  private readonly catalogService   = inject(CatalogService);
  private readonly feeService       = inject(FeeService);
  private readonly receiptService   = inject(ReceiptService);
  private readonly assemblyService  = inject(AssemblyService);
  private readonly userNoticeService = inject(UserNoticeService);
  private readonly userChargeService = inject(UserChargeService);
  private readonly agreementService = inject(AgreementService);
  private readonly personService    = inject(PersonService);
  private readonly userService      = inject(UserService);
  private readonly houseService     = inject(HouseService);
  private readonly dialog           = inject(MatDialog);

  public detailsForm: FormGroup = this.fb.group({});
  public noticeForm:  FormGroup = this.fb.group({});
  public chargeForm:  FormGroup = this.fb.group({});
  public paymentForm: FormGroup = this.fb.group({});

  displayColumns:         string[] = ['noFolio','fecha','concepto','total','conceptoPayment','montoRecibido','montoAplicado','anio'];
  displayColumnsNotify:   string[] = ['tipo','aviso','comentario','estatus','responsable'];
  displayColumnsAssembly: string[] = ['dateS','asistencia','observaciones'];
  displayColumnsCharge:   string[] = ['concepto','descripcion','monto','fechaStr','montoPagado','montoCondonado','saldo','estatusPago'];
  displayColumnsAgreement: string[] = ['noFolio','fechaStr','motivo','montoCondonadoTotal','estatusConvenio'];

  // Totales de la tabla de Cargos / Multas (fila de pie de tabla)
  totalMonto      = 0;
  totalPagado     = 0;
  totalCondonado  = 0;
  totalSaldo      = 0;

  dataSource         = new MatTableDataSource<any>();
  dataSourceNotify   = new MatTableDataSource<WaterUserNotifyModel>();
  dataSourceAssembly = new MatTableDataSource<AssemblyModel>();
  dataSourceCharge   = new MatTableDataSource<WaterUserChargeModel>();
  dataSourceAgreement = new MatTableDataSource<WaterAgreementModel>();

  usuario!: WaterUserModel;
  user!:    WaterUserDetailModel;
  person!:  PersonModel;

  @ViewChild(MatPaginator) paginator!:       MatPaginator;
  @ViewChild(MatPaginator) paginatorNotify!: MatPaginator;
  @ViewChild(MatPaginator) paginatorCharge!: MatPaginator;

  listFee: FeeModel[]       = [];
  amounts: FeeAmountModel[] = [];

  // Catálogos por clave — sin IDs hardcodeados
  secciones:     CatalogOptionModel[] = [];
  frecuencias:   CatalogOptionModel[] = [];
  estatusPago:   CatalogOptionModel[] = [];
  estatusComite: CatalogOptionModel[] = [];
  estatusToma:   CatalogOptionModel[] = [];
  estatusAviso:  CatalogOptionModel[] = [];
  conceptosCargo: CatalogOptionModel[] = [];
  tiposAviso:      CatalogOptionModel[] = [];
  responsablesPendiente: CatalogOptionModel[] = [];
  calles:          CatalogOptionModel[] = [];

  // Domicilio / Casa: cascada Calle -> Casa + mapa + vecinos de la misma casa
  allHouses:        WaterHouseModel[] = [];
  casasDeCalle:     WaterHouseModel[] = [];
  casaSeleccionada: WaterHouseModel | null = null;

  readonly DEFAULT_COORDS: google.maps.LatLngLiteral = { lat: 21.04386, lng: -101.56864 };
  mapCenter: google.maps.LatLngLiteral = this.DEFAULT_COORDS;
  mapMarker: google.maps.LatLngLiteral = this.DEFAULT_COORDS;
  mapZoom = 17;

  displayColumnsRoommates: string[] = ['noUsuario', 'nombreCompleto'];

  ngOnInit(): void {
    this.detailsForm = this.fb.group({
      fkIdCuota:          [''],
      fkFrecuenciaPagoId: ['', Validators.required],
      estatusPagoId:      [''],
      estatusComiteId:    [''],
      fkEstatusTomaId:    [''],
      noUsuario:          ['', Validators.required],
      habitaDomicilio:    ['', Validators.required],
      tieneToma:          ['', Validators.required],
      inmuebleRenta:      ['', Validators.required],
      observaciones:      [''],
      casaNo:             [''],
      domicilioCalleId:   [''],
      grupoId:            [''],
      email:              [''],
      nombre:             ['', Validators.required],
      nombre2:            [''],
      app:                ['', Validators.required],
      apm:                [''],
      fkIdSeccion:        ['', Validators.required],
      calle:              ['', Validators.required],
      numero:             ['', Validators.required],
      referencia:         [''],
      entrecalle1:        [''],
      entrecalle2:        [''],
    });

    this.noticeForm = this.fb.group({
      aviso:          ['', Validators.required],
      comentario:     [''],
      avisoEstatusId: [''],
      tipoId:         [''],
      responsableId:  ['']
    });

    this.chargeForm = this.fb.group({
      conceptoId:  ['', Validators.required],
      descripcion: [''],
      monto:       ['', Validators.required],
      fecha:       ['', Validators.required],
      comentario:  ['']
    });

    this.paymentForm = this.fb.group({
      aguaUsuarioCargoId: ['', Validators.required],
      noFolio:            ['', Validators.required],
      montoAplicado:      ['', Validators.required]
    });

    this.loadCatalogs();
    this.getAmounts();
    this.loadHouses();

    this.activatedRoute.queryParams.subscribe(params => {
      if (params?.['element']) {
        this.usuario = JSON.parse(params['element']);
        this.user    = JSON.parse(params['element']);
      }
      this.getUserDetails();
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
    this.catalogService.getOptionsByClave('ESTATUS_COMITES').subscribe({
      next: (opts) => this.estatusComite = opts,
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('ESTATUS_TOMA').subscribe({
      next: (opts) => this.estatusToma = opts,
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('ESTATUS_AVISO').subscribe({
      next: (opts) => this.estatusAviso = opts,
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('CONCEPTO_CARGO_EXTRA').subscribe({
      next: (opts) => this.conceptosCargo = opts,
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('TIPO_AVISO').subscribe({
      next: (opts) => this.tiposAviso = opts,
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('RESPONSABLE_PENDIENTE').subscribe({
      next: (opts) => this.responsablesPendiente = opts,
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptions(15).subscribe({
      next: (opts) => this.calles = opts,
      error: (e: any) => console.error(e)
    });
  }

  private loadHouses(): void {
    this.houseService.getListWaterHouse().subscribe({
      next: (resp: any) => {
        if (resp.metadata?.code === '00') {
          this.allHouses = resp.data;
          this.syncDomicilio();
        }
      },
      error: (e: any) => console.error('Error al cargar casas', e)
    });
  }

  // Reconstruye la cascada Calle -> Casa a partir de la casa ya asignada al
  // usuario (u.casaId). Se llama tanto al terminar de cargar las casas como
  // al terminar de cargar el detalle del usuario, ya que pueden resolver en
  // cualquier orden.
  private syncDomicilio(): void {
    if (!this.allHouses.length || !this.user?.casaId) return;
    const house = this.allHouses.find(h => h.casaId === this.user.casaId);
    if (!house) return;

    this.casasDeCalle = this.allHouses.filter(h => h.calleId === house.calleId);
    this.detailsForm.patchValue({
      domicilioCalleId: house.calleId,
      casaNo: house.casaId
    }, { emitEvent: false });
    this.selectCasa(house);
  }

  onCalleChange(calleId: number | null): void {
    this.casasDeCalle = calleId != null ? this.allHouses.filter(h => h.calleId === calleId) : [];
    this.detailsForm.patchValue({ casaNo: null });
    this.selectCasa(null);
  }

  onCasaChange(casaId: number | null): void {
    const house = casaId != null ? this.allHouses.find(h => h.casaId === casaId) || null : null;
    this.detailsForm.patchValue({ casaNo: casaId });
    this.selectCasa(house);
  }

  private selectCasa(house: WaterHouseModel | null): void {
    this.casaSeleccionada = house;
    const coords = (house?.lat && house?.lng) ? { lat: house.lat, lng: house.lng } : this.DEFAULT_COORDS;
    this.mapCenter = coords;
    this.mapMarker = coords;
  }

  // Otros usuarios que viven en la misma casa (excluye al usuario actual)
  get vecinos(): (WaterUserModel & { nombreCompleto: string })[] {
    const lista = this.casaSeleccionada?.listWaterUser ?? [];
    return lista
      .filter((u: any) => String(u.noUsuario) !== String(this.usuario?.noUsuario))
      .map((u: any) => ({
        ...u,
        nombreCompleto: `${u.person?.nombre ?? ''} ${u.person?.nombre2 ?? ''} ${u.person?.app ?? ''} ${u.person?.apm ?? ''}`.replace(/\s+/g, ' ').trim()
      }));
  }

  private getAmounts(): void {
    this.feeService.getFeeAmount().subscribe({
      next: (v: any) => {
        this.listFee = v.data;
        this.listFee.forEach((fee) => {
          fee.amount.forEach((amount) => {
            amount.uso  = fee.uso.nombre;
            amount.type = fee.userType.nombre;
            this.amounts.push(amount);
          });
        });
      },
      error: (e: any) => console.error(e)
    });
  }

  getReceipt(): void {
    this.receiptService.getReceiptByNoUser(this.usuario.noUsuario).subscribe({
      next: (v: any) => this.processReceiptResponse(v),
      error: (e: any) => console.error(e)
    });
  }

  processReceiptResponse(resp: any): void {
    if (resp.metadata[0].code !== '00') return;
    const flatData: any[] = [];
    (resp.data as WaterReceiptModel[]).forEach(recibo => {
      recibo.waterReceiptPayment.forEach(pago => {
        flatData.push({
          ...pago,
          fechaPago:       recibo.fecha,
          noFolio:         recibo.noFolio,
          conceptoReceipt: recibo.concepto,
          observaciones:   recibo.observaciones,
          total:           recibo.total,
          waterUser:       recibo.waterUser
        });
      });
    });
    this.dataSource = new MatTableDataSource<any>(flatData);
    this.dataSource.paginator = this.paginator;
  }

  getAssembly(): void {
    this.assemblyService.getAssemblyByNoUser(this.usuario.noUsuario).subscribe({
      next: (v: any) => this.processAssemblyResponse(v),
      error: (e: any) => console.error(e)
    });
  }

  processAssemblyResponse(resp: any): void {
    if (resp.metadata[0].code !== '00') return;
    const dataAssembly: AssemblyModel[] = resp.data;
    this.dataSourceAssembly = new MatTableDataSource<AssemblyModel>(dataAssembly);
    this.dataSourceAssembly.paginator = this.paginator;
  }

  getNotify(): void {
    this.userNoticeService.getUsersNotice(this.usuario.noUsuario).subscribe({
      next: (v: any) => this.processNotifyResponse(v),
      error: (e: any) => console.error(e)
    });
  }

  private processNotifyResponse(resp: any): void {
    if (resp.metadata[0].code !== '00') return;
    const dataNotify: WaterUserNotifyModel[] = resp.data;
    this.dataSourceNotify = new MatTableDataSource<WaterUserNotifyModel>(dataNotify);
    this.dataSourceNotify.paginator = this.paginatorNotify;
  }

  onSaveNotice(): void {
    const form = this.noticeForm.value;
    const data = {
      noUsuario:      this.usuario.noUsuario,
      aviso:          form.aviso,
      comentario:     form.comentario,
      avisoEstatusId: form.avisoEstatusId || null,
      tipoId:         form.tipoId || null,
      responsableId:  form.responsableId || null
    };
    Swal.fire({ title: 'Guardando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    this.userNoticeService.saveNotice(data).subscribe({
      next: () => {
        Swal.fire({ icon: 'success', title: 'Aviso registrado', confirmButtonText: 'Aceptar' });
        this.noticeForm.reset();
        this.getNotify();
      },
      error: () => Swal.fire({ icon: 'error', title: 'Error', text: 'Ocurrió un problema al guardar el aviso.', confirmButtonText: 'Cerrar' })
    });
  }

  getCharges(): void {
    this.userChargeService.getChargesByUser(this.usuario.noUsuario).subscribe({
      next: (v: any) => this.processChargeResponse(v),
      error: (e: any) => console.error(e)
    });
  }

  private processChargeResponse(resp: any): void {
    if (resp.metadata[0].code !== '00') return;
    const dataCharge: WaterUserChargeModel[] = resp.data;
    this.dataSourceCharge = new MatTableDataSource<WaterUserChargeModel>(dataCharge);
    this.dataSourceCharge.paginator = this.paginatorCharge;
    this.calculateChargeTotals(dataCharge);
  }

  // Suma Monto, Pagado, Condonado y Saldo de todos los cargos del usuario
  // para mostrarlos en la fila de totales al pie de la tabla.
  private calculateChargeTotals(dataCharge: WaterUserChargeModel[]): void {
    this.totalMonto     = dataCharge.reduce((acc, c) => acc + (Number(c.monto) || 0), 0);
    this.totalPagado    = dataCharge.reduce((acc, c) => acc + (Number(c.montoPagado) || 0), 0);
    this.totalCondonado = dataCharge.reduce((acc, c) => acc + (Number(c.montoCondonado) || 0), 0);
    this.totalSaldo     = dataCharge.reduce((acc, c) => acc + (Number(c.saldo) || 0), 0);
  }

  onSaveCharge(): void {
    const form = this.chargeForm.value;
    const data = {
      noUsuario:   this.usuario.noUsuario,
      conceptoId:  form.conceptoId,
      descripcion: form.descripcion,
      monto:       form.monto,
      fecha:       form.fecha,
      comentario:  form.comentario
    };
    Swal.fire({ title: 'Guardando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    this.userChargeService.saveCharge(data).subscribe({
      next: () => {
        Swal.fire({ icon: 'success', title: 'Cargo registrado', confirmButtonText: 'Aceptar' });
        this.chargeForm.reset();
        this.getCharges();
      },
      error: () => Swal.fire({ icon: 'error', title: 'Error', text: 'Ocurrió un problema al guardar el cargo.', confirmButtonText: 'Cerrar' })
    });
  }

  // Un cargo puede saldarse en uno o varios abonos, cada uno con su propio
  // folio de recibo. Esto se puede llamar varias veces hasta que saldo=0.
  onAddPayment(): void {
    const form = this.paymentForm.value;
    const data = {
      noFolio:       form.noFolio,
      montoAplicado: form.montoAplicado
    };
    Swal.fire({ title: 'Guardando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    this.userChargeService.addPayment(form.aguaUsuarioCargoId, data).subscribe({
      next: () => {
        Swal.fire({ icon: 'success', title: 'Abono registrado', confirmButtonText: 'Aceptar' });
        this.paymentForm.reset();
        this.getCharges();
      },
      error: () => Swal.fire({ icon: 'error', title: 'Error', text: 'Ocurrió un problema al registrar el abono. Verifica que el folio exista.', confirmButtonText: 'Cerrar' })
    });
  }

  getAgreements(): void {
    this.agreementService.getByNoUser(this.usuario.noUsuario).subscribe({
      next: (resp: any) => this.processAgreementResponse(resp),
      error: (e: any) => console.error(e)
    });
  }

  private processAgreementResponse(resp: any): void {
    if (resp.metadata[0].code !== '00') return;
    const dataAgreement: WaterAgreementModel[] = resp.data;
    this.dataSourceAgreement = new MatTableDataSource<WaterAgreementModel>(dataAgreement);
  }

  openNewConvenioDialog(): void {
    const nombreUsuario = `${this.person?.nombre || ''} ${this.person?.app || ''}`.trim();
    const dialogRef = this.dialog.open(NewConvenioComponent, {
      width: '900px',
      data: { noUsuario: this.usuario.noUsuario, nombreUsuario }
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result === 1) {
        Swal.fire({ icon: 'success', title: 'Convenio registrado', confirmButtonText: 'Aceptar' });
        this.getAgreements();
        this.getCharges();
      } else if (result === 2) {
        Swal.fire({ icon: 'error', title: 'Error', text: 'Ocurrió un problema al guardar el convenio.', confirmButtonText: 'Cerrar' });
      }
    });
  }

  onSaveUser(): void {
    const form = this.detailsForm.value;
    const data = {
      person: {
        personaId: this.user.personaId,
        nombre:    form.nombre,
        nombre2:   form.nombre2,
        app:       form.app,
        apm:       form.apm
      },
      adress: {
        direccionId: this.user.direccionId,
        seccionId:   form.fkIdSeccion,
        calle:       form.calle,
        numero:      form.numero,
        referencia:  form.referencia,
        entrecalle1: form.entrecalle1,
        entrecalle2: form.entrecalle2
      }
    };
    this.saveData(() => this.personService.savePersonAndAddress(data));
  }

  onSaveWaterUser(): void {
    const form = this.detailsForm.value;
    const data = {
      aguaUsuarioId:   this.user.aguaUsuarioId,
      noUsuario:       form.noUsuario,
      habitaDomicilio: form.habitaDomicilio,
      tieneToma:       form.tieneToma,
      inmuebleRenta:   form.inmuebleRenta,
      observaciones:   form.observaciones,
      cuotaId:         form.fkIdCuota,
      estatusPagoId:   form.estatusPagoId,
      frecuenciaPagoId: form.fkFrecuenciaPagoId,
      estatusComiteId: form.estatusComiteId,
      estatusTomaId:   form.fkEstatusTomaId,
      casaNo:          form.casaNo,
      grupoId:         form.grupoId
    };
    this.saveData(() => this.userService.saveWaterUser(data));
  }

  private saveData(requestFn: () => Observable<any>): void {
    Swal.fire({ title: 'Guardando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    requestFn().subscribe({
      next: () => Swal.fire({ icon: 'success', title: 'Guardado correctamente', text: 'La información se actualizó.', confirmButtonText: 'Aceptar' }),
      error: () => Swal.fire({ icon: 'error', title: 'Error', text: 'Ocurrió un problema al guardar.', confirmButtonText: 'Cerrar' })
    });
  }

  getUserDetails(): void {
    this.userService.getUserDetails(this.user.usuarioId).subscribe({
      next: (resp: any) => {
        if (resp.metadata[0].code !== '00') return;
        const u = resp.data[0];
        this.user = u;
        this.person = { personaId: u.personaId, nombre: u.nombre, nombre2: u.nombre2, app: u.app, apm: u.apm };
        this.detailsForm.patchValue({
          fkIdCuota:          u.cuotaId,
          fkFrecuenciaPagoId: u.frecuenciaPagoId,
          estatusPagoId:      u.estatusPagoId,
          estatusComiteId:    u.estatusComiteId,
          fkEstatusTomaId:    u.estatusTomaId || null,
          noUsuario:          u.noUsuario,
          habitaDomicilio:    u.habitaDomicilio,
          tieneToma:          u.tieneToma,
          inmuebleRenta:      u.inmuebleRenta,
          casaNo:             u.casaId,
          grupoId:            u.grupoId,
          nombre:             u.nombre,
          nombre2:            u.nombre2,
          app:                u.app,
          apm:                u.apm,
          fkIdSeccion:        u.seccionId,
          calle:              u.calle,
          numero:             u.numero,
          referencia:         u.referencia,
          entrecalle1:        u.entrecalle1,
          entrecalle2:        u.entrecalle2
        });
        this.syncDomicilio();
      },
      error: (e: any) => console.error('Error al cargar usuario', e)
    });
  }
}
