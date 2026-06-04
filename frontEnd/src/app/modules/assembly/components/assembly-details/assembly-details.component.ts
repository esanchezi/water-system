import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { NewReceiptComponent } from 'src/app/modules/receipt/components/new-receipt/new-receipt.component';
import { AssemblyModel, AttendanceModel } from 'src/app/modules/shared/models/Assembly.model';
import { AssemblyService } from 'src/app/modules/shared/services/assembly.service';
import { AssemblyNewAttendanceComponent } from '../assembly-new-attendance/assembly-new-attendance.component';

@Component({
  selector: 'app-assembly-details',
  templateUrl: './assembly-details.component.html',
  styleUrls: ['./assembly-details.component.css']
})
export class AssemblyDetailsComponent implements OnInit{

  public detailsForm!: FormGroup;
  public dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly assemblyService = inject(AssemblyService);

  dataSource = new MatTableDataSource<AttendanceModel>();
  displayColumns: string[]=['noUser','nombre','asistencia','observaciones'];

  assembly!: AssemblyModel;

  @ViewChild(MatPaginator)
  paginator !: MatPaginator;

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      if (params && params['element']) {
        this.assembly = JSON.parse(params['element']);
      }
    });
    this.initForm();
  }

  getAssemblyById():void{
    this.assemblyService.getAssemblyById(this.assembly.asambleaId)
      .subscribe({
        next: (v) => this.processAssemblyResponse(v),
        error: (e) => console.error(e),
        complete: () => console.info('complete') 
      });
  }

  processAssemblyResponse(resp:any){
    console.log(resp);
    const dataAssembly: AttendanceModel[] = [];
    if (resp.metadata[0].code == "00"){
      resp.data.forEach((element:AttendanceModel)=> {
        dataAssembly.push(element);
      });
      this.dataSource = new MatTableDataSource<AttendanceModel>(dataAssembly);
      this.dataSource.paginator = this.paginator;
    }
  }

  openAsistenciaDialog(){
    const dialogRef = this.dialog.open(AssemblyNewAttendanceComponent,{
      width:'900px',
      data: { asambleaId: this.assembly.asambleaId } 
    });

    dialogRef.afterClosed().subscribe((result:any) => {
      if(result == 1){
        this.openSnackBar("Categoria agregada","Exito")
        this.getAssemblyById();
      }else if (result == 2){
        this.openSnackBar("Error al guardar categoria","Error")
      }
    });

  }

  openSnackBar(message:string, action:string): MatSnackBarRef<SimpleSnackBar>{
    return this.snackBar.open(message,action,{duration:2000})
  }
    
  private initForm(): void {
    this.detailsForm = this.fb.group({
      fecha: [ this.assembly.dateS,Validators.required],
      purpose: [this.assembly.purpose,Validators.required],
    });
  }
}
