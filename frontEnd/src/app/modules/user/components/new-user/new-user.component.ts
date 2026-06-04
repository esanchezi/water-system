import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { CatalogData } from 'src/app/modules/shared/models/Catalog.model';
import { FeeAmountModel, FeeModel } from 'src/app/modules/shared/models/Fee.model';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { FeeService } from 'src/app/modules/shared/services/fee.service';
import { UserService } from 'src/app/modules/shared/services/user.service';

@Component({
  selector: 'app-new-user',
  templateUrl: './new-user.component.html',
  styleUrls: ['./new-user.component.css']
})
export class NewUserComponent  implements OnInit{
 
  public userForm!: FormGroup;
  private dialogRef = inject(MatDialogRef);
  private fb = inject(FormBuilder);
  private catalogService = inject(CatalogService);
  private feeService = inject(FeeService);
  private userService = inject(UserService);

  listFee: FeeModel[]=[];
  amounts: FeeAmountModel[]=[];
  catalogData: CatalogData = {
    cat1: [],
    cat2: [],
    cat3: [],
    cat4: [],
    cat5: [],
    cat6: []
  };
  
  // Constantes para identificadores
  private readonly ID_CUOTA_DEFAULT = '3';
  private readonly ID_FRECUENCIA_DEFAULT = '54';
  private readonly ID_SECCION_DEFAULT = '1';
  
  ngOnInit(): void {
    this.initForm();
    this.getCatalogs();
    this.getAmounts();
    
  }

  private initForm(): void {
    this.userForm = this.fb.group({
      fkIdCuota: [this.ID_CUOTA_DEFAULT,Validators.required],
      fkFrecuenciaPagoId: [this.ID_FRECUENCIA_DEFAULT,Validators.required],
      estatusPagoId: [''],
      noUsuario: ['',Validators.required],
      habitaDomicilio: ['true',Validators.required],
      tieneToma: ['true',Validators.required],
      observaciones: [''],
      email: [''],
      nombre: ['',Validators.required],
      nombre2: ['',Validators.required],
      app: ['',Validators.required],
      apm: ['',Validators.required],
      fkIdSeccion: [this.ID_SECCION_DEFAULT,Validators.required],
      calle: ['',Validators.required],
      numero: ['',Validators.required],
      referencia: ['',],
      entreCalle1: ['',],
    });
  }

  onSave(){
    const data = this.prepareUserData();
    this.saveUserData(data);
  }

  onCancel(){
    //this.dialogRef.close(3);
  }

  private prepareUserData(): any {
    let data ={
      cuotaId: this.userForm.get('fkIdCuota')?.value,
      frecuenciaPagoId: this.userForm.get('fkFrecuenciaPagoId')?.value,
      estatusPagoId: this.userForm.get('estatusPagoId')?.value, // 54 anual, 55 mensual, 56 semestral
      noUsuario: this.userForm.get('noUsuario')?.value,
      habitaDomicilio: this.userForm.get('habitaDomicilio')?.value,
      tieneToma: this.userForm.get('tieneToma')?.value,
      observaciones: this.userForm.get('observaciones')?.value,
      email: this.userForm.get('email')?.value,
      person:{
          nombre: this.userForm.get('nombre')?.value,
          nombre2: this.userForm.get('nombre2')?.value,
          app: this.userForm.get('app')?.value,
          apm: this.userForm.get('apm')?.value,
      },
      adress:{
          seccionId: this.userForm.get('fkIdSeccion')?.value, // 1 LosLopez / 2 LaBarca / 3 SanIsidro / 4 Arboledas / 5 Buenavista
          calle: this.userForm.get('calle')?.value,
          numero: this.userForm.get('numero')?.value,
          referencia: this.userForm.get('referencia')?.value,
          entreCalle1: this.userForm.get('entreCalle1')?.value,
      }
    }
    return data;
  }

  private saveUserData(data: any): void {
    this.userService.saveUser(data)
      .subscribe({
        next: () => console.info('complete'),//this.dialogRef.close(1),
        error: () => this.dialogRef.close(2),
        complete: () => console.info('complete')
      });
  }

  private getCatalogs(): void {
    this.getOptionsById(1, 'cat2', 'cuotas');
    this.getOptionsById(1, 'cat1', 'seccion');
    this.getOptionsById(11, 'cat3', 'frecuencia');
    this.getOptionsById(13, 'cat4', 'estatusPago')
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
        complete: () => {
          console.log(this.listFee);
          this.listFee.forEach((fee) => {
            fee.amount.forEach((amount) => {
              amount.uso = fee.uso.nombre;
              amount.type = fee.userType.nombre; // Asigna el campo 'uso' para cada elemento de 'fee.amount'
              this.amounts.push(amount);
            });
          });
        },
        error: (e) => console.error(e),
      })
  }
}

