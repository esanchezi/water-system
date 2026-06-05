import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { AssemblyModel } from 'src/app/modules/shared/models/Assembly.model';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { FeeAmountModel, FeeModel } from 'src/app/modules/shared/models/Fee.model';
import { PersonModel } from 'src/app/modules/shared/models/Person.model';
import { WaterReceiptModel } from 'src/app/modules/shared/models/WaterReceipt.model';
import { WaterUserDetailModel, WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';
import { WaterUserNotifyModel } from 'src/app/modules/shared/models/WaterUserNotify.model';
import { AssemblyService } from 'src/app/modules/shared/services/assembly.service';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { FeeService } from 'src/app/modules/shared/services/fee.service';
import { PersonService } from 'src/app/modules/shared/services/person.service';
import { ReceiptService } from 'src/app/modules/shared/services/receipt.service';
import { UserNoticeService } from 'src/app/modules/shared/services/user.notice.service';
import { UserService } from 'src/app/modules/shared/services/user.service';
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
  private readonly personService    = inject(PersonService);
  private readonly userService      = inject(UserService);

  public detailsForm: FormGroup = this.fb.group({});

  displayColumns:         string[] = ['noFolio','fecha','concepto','total','conceptoPayment','montoRecibido','montoAplicado','anio'];
  displayColumnsNotify:   string[] = ['aviso','comentario','estatus'];
  displayColumnsAssembly: string[] = ['dateS','asistencia','observaciones'];

  dataSource         = new MatTableDataSource<any>();
  dataSourceNotify   = new MatTableDataSource<WaterUserNotifyModel>();
  dataSourceAssembly = new MatTableDataSource<AssemblyModel>();

  usuario!: WaterUserModel;
  user!:    WaterUserDetailModel;
  person!:  PersonModel;

  @ViewChild(MatPaginator) paginator!:       MatPaginator;
  @ViewChild(MatPaginator) paginatorNotify!: MatPaginator;

  listFee: FeeModel[]       = [];
  amounts: FeeAmountModel[] = [];

  // Catálogos por clave — sin IDs hardcodeados
  secciones:     CatalogOptionModel[] = [];
  frecuencias:   CatalogOptionModel[] = [];
  estatusPago:   CatalogOptionModel[] = [];
  estatusComite: CatalogOptionModel[] = [];
  estatusToma:   CatalogOptionModel[] = [];

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

    this.loadCatalogs();
    this.getAmounts();

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
      },
      error: (e: any) => console.error('Error al cargar usuario', e)
    });
  }
}
