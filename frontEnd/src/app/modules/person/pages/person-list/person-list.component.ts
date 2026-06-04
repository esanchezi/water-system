import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { PersonService } from '../../../shared/services/person.service';
import { PersonModel } from 'src/app/modules/shared/models/Person.model';

@Component({
  selector: 'app-person-list',
  templateUrl: './person-list.component.html',
  styleUrls: ['./person-list.component.css']
})
export class PersonListComponent implements OnInit{
  private readonly router = inject(Router);
  private readonly personService = inject(PersonService);
  displayColumns: string[]=['personaId','nombre','app','apm','actions'];
  dataSource = new MatTableDataSource<PersonModel>();

  @ViewChild(MatPaginator)
  paginator !: MatPaginator;

  ngOnInit(): void {
    this.getListHouse();
    throw new Error('Method not implemented.');
  }

  getListHouse():void{
    this.personService.getPersons()
      .subscribe({
        next: (v) => this.processListPersonResponse(v),
        error: (e) => console.error(e),
        complete: () => console.info('complete') 
    });
  }

  edit(element:any){
    this.router.navigate(['dashboard/personaDetails'],{
      queryParams: { element: JSON.stringify(element) },
      // skipLocationChange: true,
    });
  }
  
  processListPersonResponse(resp:any){
      const data: PersonModel[] = [];
      console.log(resp);
      if (resp.metadata[0].code == "00"){
        let list = resp.data;
      
        list.forEach((element:PersonModel)=> {
          data.push(element);
        });
        
        this.dataSource = new MatTableDataSource<PersonModel>(data);
        this.dataSource.paginator = this.paginator;

        this.dataSource.filterPredicate = (data: PersonModel, filter: string) => {
          const searchStr = `${data.nombre} ${data.nombre2} ${data.app} ${data.apm}`.toLowerCase();
          return searchStr.includes(filter);
        };
      }
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }
}
