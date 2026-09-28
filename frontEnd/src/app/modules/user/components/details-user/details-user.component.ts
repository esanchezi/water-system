import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, forkJoin } from 'rxjs';
import { WaterAgreementModel } from 'src/app/modules/shared/models/WaterAgreement.model';
import { AvisoAdeudoAtencionModel, AvisoAdeudoModel } from 'src/app/modules/shared/models/AvisoAdeudo.model';
import { AvisoAdeudoService } from 'src/app/modules/shared/services/aviso-adeudo.service';
import { AvisoAdeudoAtencionDialogComponent } from 'src/app/modules/shared/components/aviso-adeudo-atencion-dialog/aviso-adeudo-atencion-dialog.component';
import { AgreementService } from 'src/app/modules/shared/services/agreement.service';
import { NewConvenioComponent } from 'src/app/modules/convenio/components/new-convenio/new-convenio.component';
import { AssemblyModel } from 'src/app/modules/shared/models/Assembly.model';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { FeeModel } from 'src/app/modules/shared/models/Fee.model';
import { PersonModel } from 'src/app/modules/shared/models/Person.model';
import { WaterReceiptModel } from 'src/app/modules/shared/models/WaterReceipt.model';
import { WaterGroupModel, WaterHouseModel, WaterUserDetailModel, WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';
import { WaterUserNotifyModel } from 'src/app/modules/shared/models/WaterUserNotify.model';
import { WaterUserChargeModel } from 'src/app/modules/shared/models/WaterUserCharge.model';
import { WaterUserAnnualPaymentModel } from 'src/app/modules/shared/models/WaterUserAnnualPayment.model';
import { AssemblyService } from 'src/app/modules/shared/services/assembly.service';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { FeeService } from 'src/app/modules/shared/services/fee.service';
import { PersonService } from 'src/app/modules/shared/services/person.service';
import { ReceiptService } from 'src/app/modules/shared/services/receipt.service';
import { UserNoticeService } from 'src/app/modules/shared/services/user.notice.service';
import { UserChargeService } from 'src/app/modules/shared/services/user-charge.service';
import { UserService } from 'src/app/modules/shared/services/user.service';
import { HouseService } from 'src/app/modules/shared/services/house.service';
import { GroupService } from 'src/app/modules/shared/services/group.service';
import { WaterUserAnnualPaymentService } from 'src/app/modules/shared/services/water-user-annual-payment.service';
import { ValorGeneralService } from 'src/app/modules/shared/services/valor-general.service';
import { ValorGeneralModel, CLAVES_VALOR_GENERAL } from 'src/app/modules/shared/models/ValorGeneral.model';
import { RenunciaTemporalService } from 'src/app/modules/shared/services/renuncia-temporal.service';
import { RenunciaTemporalModel } from 'src/app/modules/shared/models/RenunciaTemporal.model';
import { RenunciaTemporalDialogComponent } from '../renuncia-temporal-dialog/renuncia-temporal-dialog.component';
import { RenunciaTemporalReconexionDialogComponent } from '../renuncia-temporal-reconexion-dialog/renuncia-temporal-reconexion-dialog.component';
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
  private readonly groupService     = inject(GroupService);
  private readonly annualPaymentService = inject(WaterUserAnnualPaymentService);
  private readonly avisoAdeudoService = inject(AvisoAdeudoService);
  private readonly valorGeneralService = inject(ValorGeneralService);
  private readonly renunciaTemporalService = inject(RenunciaTemporalService);
  private readonly dialog           = inject(MatDialog);

  public detailsForm: FormGroup = this.fb.group({});
  public noticeForm:  FormGroup = this.fb.group({});
  public chargeForm:  FormGroup = this.fb.group({});
  public paymentForm: FormGroup = this.fb.group({});
  public annualPaymentForm: FormGroup = this.fb.group({});

  displayColumns:         string[] = ['noFolio','fecha','concepto','total','conceptoPayment','montoRecibido','montoAplicado','anio'];
  displayColumnsNotify:   string[] = ['tipo','aviso','comentario','estatus','responsable'];
  displayColumnsAssembly: string[] = ['dateS','asistencia','observaciones'];
  displayColumnsCharge:   string[] = ['concepto','descripcion','monto','fechaStr','montoPagado','montoCondonado','saldo','estatusPago'];
  displayColumnsAgreement: string[] = ['noFolio','fechaStr','motivo','adeudo','fechaCompromisoPagoStr','montoCondonadoTotal','estatusConvenio'];
  displayColumnsAnnualPayment: string[] = ['anio','fechaValidacion','observaciones','estatus','acciones'];
  displayColumnsRenuncia: string[] = ['folio','fechaRenuncia','motivo','adeudoALaFecha','estado','acciones'];

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
  dataSourceRenuncia = new MatTableDataSource<RenunciaTemporalModel>();

  // Renuncia temporal ACTIVA (sin reconectar todavía), si la hay -- se
  // usa para mostrar el aviso arriba del historial y decidir si se
  // ofrece "Solicitar renuncia" o "Registrar reconexión".
  renunciaActiva: RenunciaTemporalModel | null = null;

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
  // Valores generales (multa por falta de pago, corte/reconexión, aviso,
  // válvulas) -- para sugerir el monto vigente al elegir uno de esos
  // conceptos en "Nuevo cargo" (ver onConceptoCargoChange()).
  valoresGenerales: ValorGeneralModel[] = [];
  tiposAviso:      CatalogOptionModel[] = [];
  responsablesPendiente: CatalogOptionModel[] = [];
  calles:          CatalogOptionModel[] = [];
  // Giro del negocio -- catálogo NEGOCIO (id 7 en catalogo_maestro), se
  // administra desde el módulo de Catálogos.
  girosNegocio:    CatalogOptionModel[] = [];
  // Tipo de usuario -- catálogo TIPO_USUARIO (id 6): familia, viuda/o,
  // casa deshabitada, toma sin conectar, etc. Reemplaza los antiguos
  // checkboxes familiaCompleta/viudoPadreMadreSoltero.
  tiposUsuario:    CatalogOptionModel[] = [];
  // Lista de grupos (comité/zona) para el selector -- antes era un input de
  // texto libre con el id, ahora se elige de la lista real.
  grupos:          WaterGroupModel[] = [];

  // Domicilio / Casa: cascada Sección -> Calle -> Casa + mapa + vecinos
  allHouses:        WaterHouseModel[] = [];
  casasDeCalle:     WaterHouseModel[] = [];
  casaSeleccionada: WaterHouseModel | null = null;
  callesDeSeccionDomicilio: CatalogOptionModel[] = [];

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
      // Sin default: null significa "todavía no se elige", para no
      // confundir "sin contestar" con "confirmado doméstico".
      esNegocio:          [null],
      giroNegocioId:      [''],
      tieneLocal:         [false],
      localRentadoPorUsuario: [false],
      tipoUsuarioId:      [''],
      esTiendaAbarrotes:  [false],
      negocioAtendidoPorUsuario: [false],
      negocioGrande:      [false],
      alias:              [''],
      observaciones:      [''],
      casaNo:             [''],
      domicilioSeccionId: [''],
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

    this.loadCatalogs();
    this.loadGrupos();
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

  private loadGrupos(): void {
    this.groupService.getListWaterGroup().subscribe({
      next: (resp: any) => {
        // OJO: waterGroup usa BaseRestResponse (metadata como objeto), no
        // como lista -- distinto del resto de los endpoints de este
        // proyecto que usan RestResponse (metadata como arreglo).
        if (resp.metadata?.code === '00') {
          this.grupos = resp.data || [];
        }
      },
      error: (e: any) => console.error('Error al cargar grupos', e)
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
    this.valorGeneralService.getAll().subscribe({
      next: (resp: any) => { if (resp.metadata?.[0]?.code === '00') this.valoresGenerales = resp.data || []; },
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
      next: (opts) => {
        this.calles = [...opts].sort((a, b) => a.nombre.localeCompare(b.nombre));
        this.callesDeSeccionDomicilio = this.calles;
      },
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('NEGOCIO').subscribe({
      next: (opts) => this.girosNegocio = opts,
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptionsByClave('TIPO_USUARIO').subscribe({
      next: (opts) => this.tiposUsuario = [...opts].sort((a, b) => a.nombre.localeCompare(b.nombre)),
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

  // Reconstruye la cascada Sección -> Calle -> Casa a partir de la casa ya
  // asignada al usuario (u.casaId). Se llama tanto al terminar de cargar
  // las casas como al terminar de cargar el detalle del usuario, ya que
  // pueden resolver en cualquier orden.
  private syncDomicilio(): void {
    if (!this.allHouses.length || !this.user?.casaId) return;
    const house = this.allHouses.find(h => h.casaId === this.user.casaId);
    if (!house) return;

    const calleDeLaCasa = this.calles.find(c => c.catalogoOpcionesId === house.calleId);
    const seccionId = calleDeLaCasa?.zonaId ?? null;
    this.callesDeSeccionDomicilio = seccionId != null
      ? this.calles.filter(c => c.zonaId === seccionId)
      : this.calles;

    this.casasDeCalle = this.allHouses.filter(h => h.calleId === house.calleId);
    this.detailsForm.patchValue({
      domicilioSeccionId: seccionId,
      domicilioCalleId: house.calleId,
      casaNo: house.casaId
    }, { emitEvent: false });
    this.selectCasa(house);
  }

  // Primero se elige la Sección; la Calle se filtra a las que ya tienen
  // esa sección asignada (mismo patrón que en Nueva Casa).
  onDomicilioSeccionChange(seccionId: number | null): void {
    this.callesDeSeccionDomicilio = seccionId != null
      ? this.calles.filter(c => c.zonaId === seccionId)
      : this.calles;
    this.detailsForm.patchValue({ domicilioCalleId: null });
    this.onCalleChange(null);
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

  // Si el concepto elegido corresponde a uno de los valores generales
  // (multa por falta de pago, corte/reconexión, aviso, multa de válvulas --
  // ver ValorGeneralClave en el backend), sugiere el monto vigente del año
  // en curso llenando el campo "monto" -- solo si todavía está vacío, para
  // no pisar un monto que ya se haya escrito a mano (ej. la multa de
  // válvulas es un rango, el monto configurado es solo referencia).
  onConceptoCargoChange(conceptoId: number): void {
    const concepto = this.conceptosCargo.find(c => c.catalogoOpcionesId === conceptoId);
    if (!concepto || this.chargeForm.value.monto) return;

    const claveMatch = CLAVES_VALOR_GENERAL.find(cv => cv.nombre === concepto.nombre);
    if (!claveMatch) return;

    const anioActual = new Date().getFullYear();
    const vigente = this.valoresGenerales
      .filter(v => v.clave === claveMatch.clave && v.vigencia <= anioActual)
      .sort((a, b) => b.vigencia - a.vigencia)[0];
    if (vigente) {
      this.chargeForm.patchValue({ monto: vigente.monto });
    }
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

  // Árbol de clasificación de uso (ver detalles-user.component.html):
  // 1. Estatus de la toma -- si "Sin conexión", no se pregunta nada más.
  // 2/3. Si está conectada, se pregunta habita domicilio / es renta.
  // 4. Si habita domicilio -> uso doméstico automático (censo de personas
  //    habilitado), Y además se puede marcar aparte si ahí mismo también
  //    opera un negocio (ej. usuario que vive en su casa y atiende una
  //    tienda de abarrotes ahí -- no son mutuamente excluyentes).
  // 6. Si es renta (y no habita) -> sí se pregunta doméstico/negocio, ahí
  //    sí son mutuamente excluyentes (una renta se ocupa de una forma u otra).
  // 5/7. Doméstico -> habilita censo de personas.
  // 8. Negocio -> habilita "censo de negocio" (giro + tamaño), sea por la
  //    rama de renta o por el negocio adicional en la casa propia.
  get tomaSinConexion(): boolean {
    const id = Number(this.detailsForm?.value?.fkEstatusTomaId);
    if (!id) return false;
    const nombre = (this.estatusToma.find(e => e.catalogoOpcionesId === id)?.nombre || '').trim().toLowerCase();
    return nombre === 'sin conexión' || nombre === 'sin conexion';
  }

  // Elección doméstico/negocio mutuamente excluyente -- solo aplica en la
  // rama de renta (ahí sí es una cosa u otra, no ambas).
  get mostrarUsoToggle(): boolean {
    const f = this.detailsForm?.value;
    return !!f?.inmuebleRenta && !f?.habitaDomicilio;
  }

  // Checkbox independiente de "es negocio" -- se muestra en cualquier caso
  // que NO sea la rama de renta-sin-habitar (ahí ya hay un radio
  // doméstico/negocio excluyente más abajo). Cubre: vive aquí y también
  // tiene una tiendita, Y el caso de toma conectada sin habitar y sin ser
  // renta pero que sí es un negocio (ej. bodega/local que el mismo dueño
  // usa, sin vivir ahí ni rentarlo a nadie).
  get mostrarNegocioIndependiente(): boolean {
    const f = this.detailsForm?.value;
    if (!f || this.tomaSinConexion) return false;
    return !(f.inmuebleRenta && !f.habitaDomicilio);
  }

  // esNegocio puede ser null ("todavía no se elige") en la rama de renta --
  // solo cuenta como doméstico o negocio cuando se elige explícitamente,
  // para no dar por hecho una respuesta que nadie confirmó.
  get esUsoDomestico(): boolean {
    const f = this.detailsForm?.value;
    if (!f) return false;
    if (f.habitaDomicilio) return true;
    if (f.inmuebleRenta) return f.esNegocio === false;
    return false;
  }

  // Negocio: no depende de habitaDomicilio/inmuebleRenta -- basta con que
  // esté marcado explícitamente, ya sea junto con habitar el domicilio, en
  // la rama de renta (vía el radio), o solo, sin habitar ni ser renta.
  get esUsoNegocio(): boolean {
    return this.detailsForm?.value?.esNegocio === true;
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

    if (!this.esUsoNegocio) {
      // Uso doméstico -- sugerencia según el tipo de usuario seleccionado
      // (catálogo TIPO_USUARIO). Solo cubre los nombres de opción más
      // comunes hoy; para el resto (casa deshabitada, toma sin conectar,
      // jardín, animales, etc.) no propone nada -- se decide manualmente.
      const tipo = (this.tiposUsuario.find(t => t.catalogoOpcionesId === Number(f.tipoUsuarioId))?.nombre || '').trim().toLowerCase();
      if (tipo === 'familia') return 'Cuota completa';
      if (tipo === 'viuda / viudo' || tipo === 'madre/padre soltero' || tipo === '3ra edad') return 'Media cuota';
      if (tipo.startsWith('usuario solo comparte toma')) return 'No requiere cuota propia: agregar como integrante de una familia ya registrada en este domicilio';
      return 'Selecciona el tipo de usuario para ver la sugerencia (o decide manualmente si no aplica ninguno de los casos comunes)';
    }

    // Negocio
    if (f.esTiendaAbarrotes) return 'No se cobra (exento)';
    if (f.negocioAtendidoPorUsuario) return 'Un cuarto de cuota';
    if (f.negocioGrande) return 'A decisión del comité / asamblea';
    return 'Media cuota';
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
      tipoUsuarioId:   form.tipoUsuarioId || null,
      esTiendaAbarrotes: form.esTiendaAbarrotes,
      negocioAtendidoPorUsuario: form.negocioAtendidoPorUsuario,
      negocioGrande:   form.negocioGrande,
      alias:           form.alias,
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
        // "usuario" (el que recibe <app-user-censo>) se había quedado
        // fijo con lo que llegó por query param al entrar a la página --
        // si ese objeto no traía aguaUsuarioId bien puesto (depende de
        // desde qué lista se navegó aquí), el censo mandaba peticiones a
        // ".../waterUserCensus/undefined" (400 Bad Request). Aquí se
        // sincroniza con el dato recién confirmado por el backend, que es
        // el mismo que ya usan los pagos anuales más abajo.
        this.usuario = u;
        this.person = { personaId: u.personaId, nombre: u.nombre, nombre2: u.nombre2, app: u.app, apm: u.apm };
        this.revisarAvisosAdeudoPendientes(u.aguaUsuarioId, u.noUsuario);
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
          esNegocio:          u.esNegocio ?? null,
          giroNegocioId:      u.giroNegocioId || null,
          tieneLocal:         u.tieneLocal || false,
          localRentadoPorUsuario: u.localRentadoPorUsuario || false,
          tipoUsuarioId:      u.tipoUsuarioId || null,
          esTiendaAbarrotes:  u.esTiendaAbarrotes || false,
          negocioAtendidoPorUsuario: u.negocioAtendidoPorUsuario || false,
          negocioGrande:      u.negocioGrande || false,
          alias:              u.alias || '',
          casaNo:             u.casaId,
          grupoId:            u.grupoId || null,
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

  // Si este usuario ya tiene alguna carta de adeudo entregada y todavía
  // sin marcar como atendida, se alerta cada vez que se consulta su ficha
  // -- a razón de realizar el cobro correspondiente. La alerta se repite
  // en cada consulta mientras no se marque atendida (no es un aviso
  // "visto una vez", es un pendiente activo).
  private revisarAvisosAdeudoPendientes(aguaUsuarioId: number, noUsuario: number): void {
    if (!aguaUsuarioId) {
      this.revisarNotasPendientes(noUsuario);
      return;
    }
    this.avisoAdeudoService.getPendientesDeAtencion(aguaUsuarioId).subscribe({
      next: (resp: any) => {
        if (resp.metadata?.code !== '00') {
          this.revisarNotasPendientes(noUsuario);
          return;
        }
        const pendientes: AvisoAdeudoModel[] = resp.data || [];
        if (pendientes.length === 0) {
          this.revisarNotasPendientes(noUsuario);
          return;
        }

        const detalle = pendientes.map(a => {
          const tipo = a.tipoAviso === 'SEGUNDO' ? 'Segundo aviso' : 'Primer aviso';
          // Texto plano (sin pasar por Date/zona horaria) -- fechaEntrega es
          // una fecha "naive", ver aviso-adeudo-entrega-dialog.component.ts.
          const fecha = a.fechaEntrega ? this.formatearFechaNaive(a.fechaEntrega) : '--';
          return `<li>Folio ${a.folioNotificacion} (${tipo}) -- entregada el ${fecha}</li>`;
        }).join('');

        Swal.fire({
          icon: 'warning',
          title: 'Carta de adeudo entregada',
          html: `<p style="text-align:left">Este usuario ya recibió su carta de adeudo y sigue pendiente de realizar el cobro correspondiente:</p>
                 <ul style="text-align:left">${detalle}</ul>`,
          showCancelButton: true,
          confirmButtonText: 'Ya se hizo el cobro (marcar atendida)',
          cancelButtonText: 'Cerrar'
        }).then(result => {
          if (!result.isConfirmed) {
            this.revisarNotasPendientes(noUsuario);
            return;
          }
          // Se pide cómo se resolvió (pagado/condonado/convenio/otro) antes
          // de marcar -- mismo dato para todos los pendientes de este
          // usuario, ya que normalmente un solo pago/trámite los resuelve
          // en conjunto. Ver AvisoAdeudoAtencionDialogComponent.
          const dialogRef = this.dialog.open(AvisoAdeudoAtencionDialogComponent, {
            width: '420px',
            data: { avisos: pendientes }
          });
          dialogRef.afterClosed().subscribe((datos: AvisoAdeudoAtencionModel | null) => {
            if (!datos) {
              this.revisarNotasPendientes(noUsuario);
              return;
            }
            forkJoin(pendientes.map(a => this.avisoAdeudoService.marcarAtendida(a.avisoAdeudoId, datos))).subscribe({
              next: () => {
                Swal.fire('Listo', 'Se marcó como atendida.', 'success').then(() => this.revisarNotasPendientes(noUsuario));
              },
              error: (e: any) => {
                console.error(e);
                const mensaje = e?.error?.metadata?.message || 'No se pudo marcar como atendida, intenta de nuevo.';
                Swal.fire('Error', mensaje, 'error').then(() => this.revisarNotasPendientes(noUsuario));
              }
            });
          });
        });
      },
      error: (e: any) => {
        console.error('Error al consultar avisos de adeudo pendientes', e);
        this.revisarNotasPendientes(noUsuario);
      }
    });
  }

  // Alerta persistente de avisos/notas (tabla agua_usuario_aviso, distinta
  // de las cartas de adeudo) que siguen en estatus "Pendiente" -- se
  // muestra cada vez que se consulta la ficha del usuario, hasta que se
  // marquen como "Atendido" (mismo patrón que revisarAvisosAdeudoPendientes,
  // encadenado después de esa para que no se empalmen los Swal).
  private revisarNotasPendientes(noUsuario: number): void {
    if (!noUsuario) return;
    this.userNoticeService.getUsersNotice(noUsuario).subscribe({
      next: (resp: any) => {
        if (resp.metadata?.code !== '00' && resp.metadata?.[0]?.code !== '00') return;
        const notas: WaterUserNotifyModel[] = resp.data || [];
        const pendientes = notas.filter(n => n.estatusAviso?.nombre === 'Pendiente');
        if (pendientes.length === 0) return;

        const detalle = pendientes.map(n => {
          const tipo = n.tipo?.nombre ? `${n.tipo.nombre}: ` : '';
          return `<li>${tipo}${n.aviso}${n.comentario ? ' -- ' + n.comentario : ''}</li>`;
        }).join('');

        Swal.fire({
          icon: 'warning',
          title: 'Avisos/notas pendientes',
          html: `<p style="text-align:left">Este usuario tiene avisos/notas sin resolver:</p>
                 <ul style="text-align:left">${detalle}</ul>`,
          showCancelButton: true,
          confirmButtonText: 'Marcar como resueltas',
          cancelButtonText: 'Cerrar'
        }).then(result => {
          if (!result.isConfirmed) return;
          const atendido = this.estatusAviso.find(e => e.nombre === 'Atendido');
          if (!atendido) {
            Swal.fire('Error', 'No se encontró el estatus "Atendido" en el catálogo.', 'error');
            return;
          }
          forkJoin(pendientes.map(n => this.userNoticeService.updateEstatus(n.aguaUsuarioAvisoId!, atendido.catalogoOpcionesId))).subscribe({
            next: () => {
              Swal.fire('Listo', 'Se marcaron como resueltas.', 'success');
              this.getNotify();
            },
            error: (e: any) => {
              console.error(e);
              Swal.fire('Error', 'No se pudo actualizar, intenta de nuevo.', 'error');
            }
          });
        });
      },
      error: (e: any) => console.error('Error al consultar avisos/notas pendientes', e)
    });
  }

  // --- Renuncia temporal al servicio (Art. 6 Bis) ---

  getHistorialRenuncia(): void {
    if (!this.usuario?.aguaUsuarioId) return;
    this.renunciaTemporalService.getHistorialPorUsuario(this.usuario.aguaUsuarioId).subscribe({
      next: (resp: any) => {
        if (resp.metadata?.code !== '00') return;
        const historial: RenunciaTemporalModel[] = resp.data || [];
        this.dataSourceRenuncia = new MatTableDataSource<RenunciaTemporalModel>(historial);
        this.renunciaActiva = historial.find(r => !r.reconectado) || null;
      },
      error: (e: any) => console.error('Error al consultar historial de renuncia temporal', e)
    });
  }

  onSolicitarRenuncia(): void {
    const dialogRef = this.dialog.open(RenunciaTemporalDialogComponent, {
      width: '700px',
      data: {
        aguaUsuarioId: this.usuario.aguaUsuarioId,
        noUsuario:     this.usuario.noUsuario,
        nombreUsuario: `${this.person?.nombre || ''} ${this.person?.app || ''}`.trim()
      }
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (!result) return;
      if (!result.ok) {
        Swal.fire({ icon: 'error', title: 'Error', text: result.mensaje || 'Ocurrió un problema al generar la solicitud.', confirmButtonText: 'Cerrar' });
        return;
      }
      this.descargarBlob(result.blob, 'renuncia_temporal.pdf');
      Swal.fire({ icon: 'success', title: 'Renuncia registrada', text: 'Se generó y descargó el acta de renuncia temporal.', confirmButtonText: 'Aceptar' });
      this.getHistorialRenuncia();
    });
  }

  onReconectar(item: RenunciaTemporalModel): void {
    const dialogRef = this.dialog.open(RenunciaTemporalReconexionDialogComponent, {
      width: '700px',
      data: { renuncia: item }
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result === 1) {
        Swal.fire({ icon: 'success', title: 'Reconexión registrada', confirmButtonText: 'Aceptar' });
        this.getHistorialRenuncia();
      } else if (result === 2) {
        Swal.fire({ icon: 'error', title: 'Error', text: 'Ocurrió un problema al registrar la reconexión.', confirmButtonText: 'Cerrar' });
      }
    });
  }

  // Solo para corregir un registro dado de alta por error -- no exime el
  // adeudo previo ni reconecta al usuario por sí sola.
  onCancelarRenuncia(item: RenunciaTemporalModel): void {
    Swal.fire({
      icon: 'warning',
      title: 'Cancelar renuncia',
      text: `¿Confirmas cancelar la renuncia temporal folio ${item.folio}? Esto es solo para corregir un registro dado de alta por error.`,
      showCancelButton: true,
      confirmButtonText: 'Cancelar renuncia',
      cancelButtonText: 'Cerrar'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.renunciaTemporalService.cancelar(item.renunciaTemporalId).subscribe({
        next: () => this.getHistorialRenuncia(),
        error: (e: any) => {
          console.error(e);
          Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo cancelar el registro.', confirmButtonText: 'Cerrar' });
        }
      });
    });
  }

  // "dd/MM/yyyy" armado con puro texto -- ver aviso-adeudo-list.component.ts
  // (mismo motivo: fechaEntrega es una fecha "naive", sin zona horaria).
  private formatearFechaNaive(fechaISO: string): string {
    const [anio, mes, dia] = fechaISO.substring(0, 10).split('-');
    return `${dia}/${mes}/${anio}`;
  }

  private descargarBlob(blob: Blob, nombreArchivo: string): void {
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombreArchivo;
    enlace.click();
    URL.revokeObjectURL(url);
  }
}
