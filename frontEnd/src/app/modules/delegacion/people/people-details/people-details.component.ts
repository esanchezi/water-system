import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { FamilyMemberModel } from 'src/app/modules/shared/models/FamilyMember.model';
import { PersonModel } from 'src/app/modules/shared/models/Person.model';
import { ProcessModel } from 'src/app/modules/shared/models/Process.model';
import { FamilyMemberService } from 'src/app/modules/shared/services/familyMember.service';
import { ProcessService } from 'src/app/modules/shared/services/process.service';

@Component({
  selector: 'app-people-details',
  templateUrl: './people-details.component.html',
  styleUrls: ['./people-details.component.css']
})
export class PeopleDetailsComponent implements OnInit{

  public detailsForm!: FormGroup;
  private readonly fb = inject(FormBuilder);
  private readonly activatedRoute = inject(ActivatedRoute);
  displayColumns: string[]=['nombre','parentesco','observaciones'];
  dataSource = new MatTableDataSource<FamilyMemberModel>();
  displayColumnsProcess: string[]=['nombre','tramite','estatus','fecha','observaciones'];
  dataSourceProcess = new MatTableDataSource<ProcessModel>();
  private readonly processService = inject(ProcessService);
  private readonly famlilyService = inject(FamilyMemberService);
  
  person!: PersonModel;

  @ViewChild(MatPaginator)
  paginator !: MatPaginator;

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      if (params && params['element']) {
        this.person = JSON.parse(params['element']);
      }
    });
    this.initForm();
  }
  
  private initForm(): void {
    this.detailsForm = this.fb.group({
      email: [''],
      nombre: [ this.person.nombre,Validators.required],
      nombre2: [this.person.nombre2,Validators.required],
      app: [this.person.app,Validators.required],
      apm: [this.person.apm,Validators.required],
    });
  }

  getFamily(){
    this.famlilyService.findByIdPerson(this.person.personaId)
      .subscribe({
        next: (v) => this.familyResponse(v)
    })
  }
  
  familyResponse(resp:any){
    console.log(resp);
    const dataMember: FamilyMemberModel[] = [];
    if (resp.metadata[0].code == "00"){
        let listUser = resp.data;
  
        listUser.forEach((element:FamilyMemberModel)=> {
          dataMember.push(element);
        });
      this.dataSource = new MatTableDataSource<FamilyMemberModel>(dataMember);
      this.dataSource.paginator = this.paginator;
    }
  }

  getProcess(){
    this.processService.findByIdPerson(this.person.personaId)
      .subscribe({
       next: (v) => this.processResponse(v)
    })
  }
  
  processResponse(resp:any){
    console.log(resp);
    const dataProcess: ProcessModel[] = [];
    if (resp.metadata[0].code == "00"){
        let listUser = resp.data;
        listUser.forEach((element:ProcessModel)=> {
          dataProcess.push(element);
        });
      this.dataSourceProcess = new MatTableDataSource<ProcessModel>(dataProcess);
      this.dataSourceProcess.paginator = this.paginator;
    }
  }

}
