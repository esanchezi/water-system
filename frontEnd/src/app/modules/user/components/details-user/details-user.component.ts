import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { WaterAgreementModel } from 'src/app/modules/shared/models/WaterAgreement.model';
import { AgreementService } from 'src/app/modules/shared/services/agreement.service';
import { NewConvenioComponent } from 'src/app/modules/convenio/components/new-convenio/new-convenio.component';
import { AssemblyModel } from 'src/app/modules/shared/models/Assembly.model';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { FeeModel } from 'src/app/modules/shared/models/Fee.model';
import { PersonModel } from 'src/app/modules/shared/models/Person.model';
import { WaterReceiptModel } from 'src/app/modules/shared/models/WaterReceipt.model';
import { WaterHouseModel, WaterUserDetailModel, WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';
import { WaterUserNotifyModel } from 'src/app/modules/shared/models/WaterUserNotify.model';
import { WaterUserChargeModel } from 'src/app/modules/shared/models/WaterUserCharge.model';
import { WaterUserAnnualPaymentModel } from 'src/app/modules/shared/models/WaterUserAnnualPayment.model';
import { WaterUserCensusModel } from 'src/app/modules/shared/models/WaterUserCensus.model';
import { AssemblyService } from 'src/app/modules/shared/services/assembly.service';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { FeeService } from 'src/app/modules/shared/services/fee.service';
import { PersonService } from 'src/app/modules/shared/services/person.service';
import { ReceiptService } from 'src/app/modules/shared/services/receipt.service';
import { UserNoticeService } from 'src/app/modules/shared/services/user.notice.service';
import { UserChargeService } from 'src/app/modules/shared/services/user-charge.service';
import { UserService } from 'src/app/modules/shared/services/user.service';
import { HouseService } from 'src/app/modules/shared/services/house.service';
import { WaterUserAnnualPaymentService } from 'src/app/modules/shared/services/water-user-annual-payment.service';
import { WaterUserCensusService } from 'src/app/modules/shared/services/water-user-census.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-details-user',
  templateUrl: './details-user.component.html',
  styleUrls: ['./details-user.component.css']
})
export class DetailsUserComponent implements OnInit {

  private readonly fb               = inject(FormBuilder);
  private readonly activatedRoute   = inject(ActivatedRoute);
  private readonly router           = inject(Router);
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
  private readonly annualPaymentService = inject(WaterUserAnnualPaymentService);
  private readonly censusService    = inject(WaterUserCensusService);
  private readonly dialog           = inject(MatDialog);

  public detailsForm: FormGroup = this.fb.group({});
  public noticeForm:  FormGroup = this.fb.group({});
  public chargeForm:  FormGroup = this.fb.group({});
  public paymentForm: FormGroup = this.fb.group({});
  public annualPaymentForm: FormGroup = this.fb.group({});
  public censusForm: FormGroup = this.fb.group({});

  displayColumns:         string[] = ['noFolio','fecha','concepto','total','conceptoPayment','montoRecibido','montoAplicado','anio'];
  displayColumnsNotify:   string[] = ['tipo','aviso','comentario','estatus','responsable'];
  displayColumnsAssembly: string[] = ['dateS','asistencia','observaciones'];
  displayColumnsCharge:   string[] = ['concepto','descripcion','monto','fechaStr','montoPagado','montoCondonado','saldo','estatusPago'];
  displayColumnsAgreement: string[] = ['noFolio','fechaStr','motivo','adeudo','fechaCompromisoPagoStr','montoCondonadoTotal','estatusConvenio'];
  displayColumnsAnnualPayment: string[] = ['anio','fechaValidacion','observaciones','estatus','acciones'];
  displayColumnsCenso: string[] = ['edadActual','observaciones','acciones'];

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
  dataSourceAnnualPayment = new MatTableDataSource<WaterUserAnnualPaymentModel>();
  dataSourceCenso = new MatTableDataSource<WaterUserCensusModel>();

  usuario!: WaterUserModel;
  user!:    WaterUserDetailModel;
  person!:  PersonModel;

  @ViewChild(MatPaginator) paginator!:       MatPaginator;
  @ViewChild(MatPaginator) paginatorNotify!: MatPaginator;
  @ViewChild(MatPaginator) paginatorCharge!: MatPaginator;

  listFee: FeeModel[]       = [];
  private readonly anioActual = new Date().getFullYear();

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
  // Giro del negocio -- catálogo opcional (clave GIRO_NEGOCIO), se crea
  // desde el módulo de Catálogos. Si no existe todavía, esta lista sale
  // vacía y el select simplemente no muestra opciones.
  girosNegocio:    CatalogOptionModel[] = [];

  // Domicilio / Casa: cascada Calle -> Casa + mapa + vecinos de la misma casa
  allHouses:        WaterHouseModel[] = [];
  casasDeCalle:     WaterHouseModel[] = [];
  casaSeleccionada: WaterHouseModel | null = null;

  readonly DEFAULT_COORDS: google.maps.LatLngLiteral = { lat: 21.04386, lng: -101.56864 };
  mapCenter: google.maps.LatLngLiteral = this.DEFAULT_COORDS;
  mapMarker: google.maps.LatLngLiteral = this.DEFAULT_COORDS;
  mapZoom = 17;
  ubicacionModificada = false;

  displayColumnsRoommates: string[] = ['noUsuario', 'nombreCompleto', 'verDetalle'];

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
      esNegocio:          [false],
      giroNegocioId:      [''],
      tieneLocal:         [false],
      localRentadoPorUsuario: [false],
      familiaCompleta:    [true],
      viudoPadreMadreSoltero: [false],
      esTiendaAbarrotes:  [false],
      negocioAtendidoPorUsuario: [false],
      negocioGrande:      [false],
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

    this.annualPaymentForm = this.fb.group({
      anio:           ['', Validators.required],
      fechaValidacion: [''],
      observaciones:  ['']
    });

    // La edad es opcional a propósito -- si no se da, la persona igual
    // cuenta en el censo, solo que aparece como "sin clasificar" en el
    // resumen por edades.
    this.censusForm = this.fb.group({
      edad:          [''],
      observaciones: ['']
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
      next: (opts) => this.calles = [...opts].sort((a, b) => a.nombre.localeCompare(b.nombre)),
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('GIRO_NEGOCIO').subscribe({
      next: (opts) => this.girosNegocio = opts,
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
    this.ubicacionModificada = false;
  }

  // Otros usuarios que viven en la misma casa (excluye al usuario actual)
  get vecinos(): (WaterUserModel & { usuarioId: number; nombreCompleto: string })[] {
    const lista = this.casaSeleccionada?.listWaterUser ?? [];
    return lista
      .filter((u: any) => String(u.noUsuario) !== String(this.usuario?.noUsuario))
      .map((u: any) => ({
        ...u,
        usuarioId: u.aguaUsuarioId,
        nombreCompleto: `${u.person?.nombre ?? ''} ${u.person?.nombre2 ?? ''} ${u.person?.app ?? ''} ${u.person?.apm ?? ''}`.replace(/\s+/g, ' ').trim()
      }));
  }

  verVecino(vecino: any): void {
    this.router.navigate(['dashboard/detailsUser'], {
      queryParams: { element: JSON.stringify(vecino) }
    });
  }

  // Permite reubicar el marcador de la casa haciendo click o arrastrando el pin
  onDomicilioMapClick(event: google.maps.MapMouseEvent): void {
    if (!this.casaSeleccionada || !event.latLng) return;
    this.mapMarker = { lat: event.latLng.lat(), lng: event.latLng.lng() };
    this.ubicacionModificada = true;
  }

  onDomicilioMarkerDragEnd(event: google.maps.MapMouseEvent): void {
    if (!this.casaSeleccionada || !event.latLng) return;
    this.mapMarker = { lat: event.latLng.lat(), lng: event.latLng.lng() };
    this.ubicacionModificada = true;
  }

  guardarUbicacion(): void {
    if (!this.casaSeleccionada) return;
    const data = {
      casaNo:       this.casaSeleccionada.casaNo,
      nombre:       this.casaSeleccionada.nombre,
      observaciones: this.casaSeleccionada.observaciones,
      lado:         this.casaSeleccionada.lado,
      calleId:      this.casaSeleccionada.calleId,
      lat:          this.mapMarker.lat,
      lng:          this.mapMarker.lng
    };
    Swal.fire({ title: 'Guardando ubicación...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    this.houseService.updateWaterHouse(this.casaSeleccionada.casaId, data).subscribe({
      next: () => {
        if (this.casaSeleccionada) {
          this.casaSeleccionada.lat = this.mapMarker.lat;
          this.casaSeleccionada.lng = this.mapMarker.lng;
          const house = this.allHouses.find(h => h.casaId === this.casaSeleccionada?.casaId);
          if (house) {
            house.lat = this.mapMarker.lat;
            house.lng = this.mapMarker.lng;
          }
        }
        this.ubicacionModificada = false;
        Swal.fire({ icon: 'success', title: 'Ubicación actualizada', confirmButtonText: 'Aceptar' });
      },
      error: () => Swal.fire({ icon: 'error', title: 'Error', text: 'Ocurrió un problema al guardar la ubicación.', confirmButtonText: 'Cerrar' })
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

  getAnnualPayments(): void {
    this.annualPaymentService.getByNoUser(this.usuario.noUsuario).subscribe({
      next: (resp: any) => this.processAnnualPaymentResponse(resp),
      error: (e: any) => console.error(e)
    });
  }

  private processAnnualPaymentResponse(resp: any): void {
    if (resp.metadata[0].code !== '00') return;
    const dataAnnualPayment: WaterUserAnnualPaymentModel[] = resp.data;
    this.dataSourceAnnualPayment = new MatTableDataSource<WaterUserAnnualPaymentModel>(dataAnnualPayment);
  }

  onSaveAnnualPayment(): void {
    const form = this.annualPaymentForm.value;
    const data = {
      anio:            form.anio,
      fechaValidacion: form.fechaValidacion || null,
      observaciones:   form.observaciones
    };
    Swal.fire({ title: 'Guardando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    this.annualPaymentService.create(this.user.aguaUsuarioId, data).subscribe({
      next: () => {
        Swal.fire({ icon: 'success', title: 'Año marcado como pagado', confirmButtonText: 'Aceptar' });
        this.annualPaymentForm.reset();
        this.getAnnualPayments();
      },
      error: (e: any) => {
        const msg = e?.error?.metadata?.[0]?.description || 'Ocurrió un problema al guardar.';
        Swal.fire({ icon: 'error', title: 'Error', text: msg, confirmButtonText: 'Cerrar' });
      }
    });
  }

  onDeactivateAnnualPayment(item: WaterUserAnnualPaymentModel): void {
    Swal.fire({
      icon: 'warning',
      title: 'Dar de baja',
      text: `¿Confirmas quitar la marca de pagado del año ${item.anio}?`,
      showCancelButton: true,
      confirmButtonText: 'Dar de baja',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (!result.isConfirmed) return;
      this.annualPaymentService.deactivate(item.pagoAnualId).subscribe({
        next: () => this.getAnnualPayments(),
        error: (e: any) => {
          console.error(e);
          Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo dar de baja el registro.', confirmButtonText: 'Cerrar' });
        }
      });
    });
  }

  // Calculadora de cuota SUGERIDA -- nunca cambia this.cuotaId sola, solo
  // propone una categoría para que la persona capturando la confirme
  // seleccionando manualmente la Cuota correspondiente arriba. El árbol
  // todavía tiene ramas pendientes de definir (granja, jardín) y una rama
  // que depende de asamblea ("a decisión del comité"), por eso es
  // deliberadamente una sugerencia y no una asignación automática.
  get sugerenciaCuota(): string {
    const f = this.detailsForm?.value;
    if (!f) return '';

    if (!f.esNegocio) {
      // Uso doméstico
      if (f.familiaCompleta) return 'Cuota completa';
      if (f.viudoPadreMadreSoltero) return 'Media cuota';
      return 'No requiere cuota propia: agregar como integrante de una familia ya registrada en este domicilio';
    }

    // Negocio
    if (f.esTiendaAbarrotes) return 'No se cobra (exento)';
    if (f.negocioAtendidoPorUsuario) return 'Un cuarto de cuota';
    if (f.negocioGrande) return 'A decisión del comité / asamblea';
    return 'Media cuota';
  }

  getCenso(): void {
    this.censusService.getByAguaUsuarioId(this.user.aguaUsuarioId).subscribe({
      next: (resp: any) => this.processCensoResponse(resp),
      error: (e: any) => console.error(e)
    });
  }

  private processCensoResponse(resp: any): void {
    if (resp.metadata[0].code !== '00') return;
    const dataCenso: WaterUserCensusModel[] = resp.data;
    this.dataSourceCenso = new MatTableDataSource<WaterUserCensusModel>(dataCenso);
  }

  onSaveCenso(): void {
    const form = this.censusForm.value;
    const data = {
      edad:          form.edad !== '' && form.edad !== null ? Number(form.edad) : null,
      observaciones: form.observaciones
    };
    this.censusService.create(this.user.aguaUsuarioId, data).subscribe({
      next: () => {
        this.censusForm.reset();
        this.getCenso();
      },
      error: (e: any) => {
        console.error(e);
        Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo agregar la persona al censo.', confirmButtonText: 'Cerrar' });
      }
    });
  }

  onDeactivateCenso(item: WaterUserCensusModel): void {
    Swal.fire({
      icon: 'warning',
      title: 'Quitar del censo',
      text: '¿Confirmas quitar a esta persona del censo (ej. ya no vive en el domicilio)?',
      showCancelButton: true,
      confirmButtonText: 'Quitar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (!result.isConfirmed) return;
      this.censusService.deactivate(item.censoId).subscribe({
        next: () => this.getCenso(),
        error: (e: any) => {
          console.error(e);
          Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo quitar el registro.', confirmButtonText: 'Cerrar' });
        }
      });
    });
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
      esNegocio:       form.esNegocio,
      giroNegocioId:   form.giroNegocioId || null,
      tieneLocal:      form.tieneLocal,
      localRentadoPorUsuario: form.localRentadoPorUsuario,
      familiaCompleta: form.familiaCompleta,
      viudoPadreMadreSoltero: form.viudoPadreMadreSoltero,
      esTiendaAbarrotes: form.esTiendaAbarrotes,
      negocioAtendidoPorUsuario: form.negocioAtendidoPorUsuario,
      negocioGrande:   form.negocioGrande,
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
          esNegocio:          u.esNegocio || false,
          giroNegocioId:      u.giroNegocioId || null,
          tieneLocal:         u.tieneLocal || false,
          localRentadoPorUsuario: u.localRentadoPorUsuario || false,
          familiaCompleta:    u.familiaCompleta ?? true,
          viudoPadreMadreSoltero: u.viudoPadreMadreSoltero || false,
          esTiendaAbarrotes:  u.esTiendaAbarrotes || false,
          negocioAtendidoPorUsuario: u.negocioAtendidoPorUsuario || false,
          negocioGrande:      u.negocioGrande || false,
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
