import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AssemblyModel } from 'src/app/modules/shared/models/Assembly.model';
import { CatalogData } from 'src/app/modules/shared/models/Catalog.model';
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
export class DetailsUserComponent implements OnInit{
 
  private readonly fb = inject(FormBuilder);
  public detailsForm: FormGroup = this.fb.group({});
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly catalogService = inject(CatalogService);
  private readonly feeService = inject(FeeService);
  private readonly receiptService = inject(ReceiptService);
  private readonly assemblyService = inject(AssemblyService);
  private readonly userNoticeService = inject(UserNoticeService);
  private readonly personService = inject(PersonService);
  private readonly userService = inject(UserService);
  displayColumns: string[]=['noFolio','fecha','concepto','total','conceptoPayment','montoRecibido','montoAplicado','anio'];
  displayColumnsNotify: string[]=['aviso','comentario','estatus'];
  displayColumnsAssembly: string[]=['dateS','asistencia','observaciones'];
  dataSource = new MatTableDataSource<any>();
  dataSourceNotify = new MatTableDataSource<WaterUserNotifyModel>();
  dataSourceAssembly = new MatTableDataSource<AssemblyModel>();
  usuario!: WaterUserModel;
  user!: WaterUserDetailModel;
  person !:PersonModel;

  @ViewChild(MatPaginator)
  paginator !: MatPaginator;
  @ViewChild(MatPaginator)
  paginatorNotify !: MatPaginator;
  listFee: FeeModel[]=[];
  amounts: FeeAmountModel[]=[];

  catalogData: CatalogData = {
    cat1: [],
    cat2: [],
    cat3: [],
    cat4: [],
    cat5: [],
    cat6: [],
  };

  ngOnInit(): void {
    this.detailsForm = this.fb.group({
      fkIdCuota: [''],
      fkFrecuenciaPagoId: ['', Validators.required],
      estatusPagoId: [''],
      estatusComiteId: [''],
      fkEstatusTomaId: [''],
      noUsuario: ['', Validators.required],
      habitaDomicilio: ['', Validators.required],
      tieneToma: ['', Validators.required],
      inmuebleRenta: ['', Validators.required],
      observaciones: [''],
      casaNo: [''],
      grupoId: [''],
      email: [''],
      nombre: ['', Validators.required],
      nombre2: [''],
      app: ['', Validators.required],
      apm: [''],
      fkIdSeccion: ['', Validators.required],
      calle: ['', Validators.required],
      numero: ['', Validators.required],
      referencia: [''],
      entrecalle1: [''],
      entrecalle2: [''],
    });

    this.getCatalogs();
    this.getAmounts();
    this.activatedRoute.queryParams.subscribe(params => {
      if (params && params['element']) {
        this.usuario = JSON.parse(params['element']);
        this.user = JSON.parse(params['element']);
      }
    });

    this.getUserDetails();
  }

  getReceipt(){
    this.receiptService.getReceiptByNoUser(this.usuario.noUsuario)
      .subscribe({
      next: (v) => this.processReceiptResponse(v)
    });
  }

  processReceiptResponse(resp:any){
    const dataUser: WaterReceiptModel[] = [];
    const flatData: any[] = [];
    if (resp.metadata[0].code == "00"){
      let listUser = resp.data;

      listUser.forEach((recibo:WaterReceiptModel)=> {
        dataUser.push(recibo);
        recibo.waterReceiptPayment.forEach(pago => {
            flatData.push({
              ...pago,
              fechaPago: recibo.fecha,
              noFolio: recibo.noFolio,
              conceptoReceipt: recibo.concepto,
              observaciones: recibo.observaciones,
              total: recibo.total,
              waterUser: recibo.waterUser
            });
          });
      });
      this.dataSource = new MatTableDataSource<any>(flatData);
      this.dataSource.paginator = this.paginator;
    }
  }

  getAssembly(){
    this.assemblyService.getAssemblyByNoUser(this.usuario.noUsuario)
        .subscribe({
          next: (v) => this.processAssemblyResponse(v)
        });
  }

  processAssemblyResponse(resp:any){
    const dataAssembly: AssemblyModel[] = [];
    if (resp.metadata[0].code == "00"){
      let listAssembly = resp.data;

      listAssembly.forEach((element:AssemblyModel)=> {
        dataAssembly.push(element);
        console.log(element);
      });
      this.dataSourceAssembly = new MatTableDataSource<AssemblyModel>(dataAssembly);
      this.dataSourceAssembly.paginator = this.paginator;
    }
  }

  getNotify() {
    this.userNoticeService.getUsersNotice(this.usuario.noUsuario)
    .subscribe({
      next: (v) => this.processNotifyResponse(v)
    })

  }

  getCenso() {
    console.log("get censo");
  }

  private getCatalogs(): void {
    this.getOptionsById(1, 'cat2', 'cuotas');
    this.getOptionsById(1, 'cat1', 'seccion');
    this.getOptionsById(11, 'cat3', 'frecuencia');
    this.getOptionsById(13, 'cat4', 'estatusPago');
    this.getOptionsById(12, 'cat5', 'estatusComite');
    this.getOptionsById(14, 'cat6', 'estatusToma');
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

  private getAmounts(){
  this.feeService.getFeeAmount()
    .subscribe({
      next: (v: any) => {
        this.listFee = v.data;
      },
      error: (e) => console.error(e),
      complete: () => {
        this.listFee.forEach((fee) => {
          fee.amount.forEach((amount) => {
            amount.uso = fee.uso.nombre;
            amount.type = fee.userType.nombre;
            this.amounts.push(amount);
          });
        });
      }
    });
}

  private processNotifyResponse(resp:any){
    const dataNotify: WaterUserNotifyModel[] = [];
    if (resp.metadata[0].code == "00"){
      let listNotify = resp.data;

      listNotify.forEach((element:WaterUserNotifyModel)=> {
        dataNotify.push(element);
      });
      this.dataSourceNotify = new MatTableDataSource<WaterUserNotifyModel>(dataNotify);
      this.dataSourceNotify.paginator = this.paginatorNotify;
    }
  }

  onSaveUser(): void {
    const form = this.detailsForm.value;
  
    const person = {
      personaId: this.user.personaId,
      nombre: form.nombre,
      nombre2: form.nombre2,
      app: form.app,
      apm: form.apm
    };
  
    const adress = {
      direccionId: this.user.direccionId,
      seccionId: form.fkIdSeccion,
      calle: form.calle,
      numero: form.numero,
      referencia: form.referencia,
      entrecalle1: form.entrecalle1,
      entrecalle2: form.entrecalle2
    };
  
    const data = { person, adress };
  
    this.saveData(() => this.personService.savePersonAndAddress(data));
  }
  
  onSaveWaterUser(): void {
    const form = this.detailsForm.value;
  
    const data = {
      aguaUsuarioId: this.user.aguaUsuarioId,
      noUsuario: form.noUsuario,
      habitaDomicilio: form.habitaDomicilio,
      tieneToma: form.tieneToma,
      inmuebleRenta: form.inmuebleRenta,
      observaciones: form.observaciones,
      cuotaId: form.fkIdCuota, // corregido
      estatusPagoId: form.estatusPagoId,
      frecuenciaPagoId: form.fkFrecuenciaPagoId,
      estatusComiteId: form.estatusComiteId,
      estatusTomaId: form.fkEstatusTomaId,
      casaNo: form.casaNo,
      grupoId: form.grupoId
    };
  
    console.log(data);
    this.saveData(() => this.userService.saveWaterUser(data));
  }
  
  private saveData(requestFn: () => Observable<any>): void {
    Swal.fire({
      title: 'Guardando...',
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading()
    });
  
    requestFn().subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Guardado correctamente',
          text: 'La información se actualizó.',
          confirmButtonText: 'Aceptar'
        });
        // this.dialogRef.close(1);
      },
      error: () => {
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'Ocurrió un problema al guardar la información.',
          confirmButtonText: 'Cerrar'
        });
        // this.dialogRef.close(2);
      }
    });
  }

getUserDetails() {
  this.userService.getUserDetails(this.user.usuarioId)
    .subscribe({
      next: (resp: any) => {
        if (resp.metadata[0].code === "00") {
          const userDetails = resp.data[0]; 
          this.user = userDetails;

          this.person = {
            personaId: userDetails.personaId,
            nombre: userDetails.nombre,
            nombre2: userDetails.nombre2,
            app: userDetails.app,
            apm: userDetails.apm
          };

          // Patch del formulario
          this.detailsForm.patchValue({
            fkIdCuota: userDetails.cuotaId,
            fkFrecuenciaPagoId: userDetails.frecuenciaPagoId,
            estatusPagoId: userDetails.estatusPagoId,
            estatusComiteId: userDetails.estatusComiteId,
            fkEstatusTomaId: userDetails.estatusTomaId || null,
            noUsuario: userDetails.noUsuario,
            habitaDomicilio: userDetails.habitaDomicilio,
            tieneToma: userDetails.tieneToma,
            inmuebleRenta: userDetails.inmuebleRenta,
            casaNo: userDetails.casaId,
            grupoId: userDetails.grupoId,
            nombre: userDetails.nombre,
            nombre2: userDetails.nombre2,
            app: userDetails.app,
            apm: userDetails.apm,
            fkIdSeccion: userDetails.seccionId,
            calle: userDetails.calle,
            numero: userDetails.numero,
            referencia: userDetails.referencia,
            entrecalle1: userDetails.entrecalle1,
            entrecalle2: userDetails.entrecalle2
          });
        }
      },
      error: (err) => console.error('Error al cargar usuario', err)
    });
}

}
