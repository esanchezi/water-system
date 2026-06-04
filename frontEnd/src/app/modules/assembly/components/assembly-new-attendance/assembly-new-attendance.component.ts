import { Component, Inject, inject, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { debounceTime, distinctUntilChanged, filter,Observable, switchMap } from 'rxjs';
import { UserAutocompleteModel, WaterUserModel } from 'src/app/modules/shared/models/WaterUser.model';
import { AssemblyService } from 'src/app/modules/shared/services/assembly.service';
import { UserService } from 'src/app/modules/shared/services/user.service';

@Component({
  selector: 'app-assembly-new-attendance',
  templateUrl: './assembly-new-attendance.component.html',
  styleUrls: ['./assembly-new-attendance.component.css']
})
export class AssemblyNewAttendanceComponent implements OnInit {

  public receiptAttendance!: FormGroup;
  private readonly userService = inject(UserService);
  private readonly assemblyService = inject(AssemblyService);
  myControl = new FormControl('');
  filteredUsers!: Observable<UserAutocompleteModel[]>;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<AssemblyNewAttendanceComponent>
  ) { }

  ngOnInit(): void {
    this.receiptAttendance = new FormGroup({
      noUsuario: new FormControl(''),
      usuario: new FormControl(''),
      observaciones: new FormControl(''),
      asistencia: new FormControl(false)
    });

    this.filteredUsers = this.receiptAttendance.get('usuario')!.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      filter(value => typeof value === 'string' && value.length >= 3),
      switchMap(value => this.userService.getUsersByNameT(value))
    );
  }

  displayFn(user: UserAutocompleteModel): string {
    return user && user.nombre ? user.nombre : '';
  }

  onUserSelected(user: UserAutocompleteModel) {
    this.receiptAttendance.patchValue({
      noUsuario: user.noUsuario
    });
  }

  onSave(){
    const data = this.prepareAttendanceData();
    this.saveAttendanceData(data);
    this.dialogRef.close(1); 
  }
  onCancel(){}

  private prepareAttendanceData(): any {
    let data ={
      asambleaId: this.data.asambleaId, 
      observaciones: this.receiptAttendance.get('observaciones')?.value,
      asistencia: this.receiptAttendance.get('asistencia')?.value,
      waterUser:{
        noUsuario: this.receiptAttendance.get('noUsuario')?.value,
      }
    }
    return data;
  }

  private saveAttendanceData(data: any): void {
    this.assemblyService.saveAttendance(data)
      .subscribe({
        next: () => console.info('complete'),//this.dialogRef.close(1),
        error: () => console.info('error'),//this.dialogRef.close(2),
        complete: () => console.info('complete')
      });
  }

}
