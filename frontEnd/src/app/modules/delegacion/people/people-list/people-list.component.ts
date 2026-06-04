import { Component,OnInit, ViewChild, inject } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';

import { PersonService } from '../../../shared/services/person.service';
import { PersonModel } from 'src/app/modules/shared/models/Person.model';
import { MatPaginator } from '@angular/material/paginator';

@Component({
  selector: 'app-people-list',
  templateUrl: './people-list.component.html',
  styleUrls: ['./people-list.component.css']
})
export class PeopleListComponent implements OnInit{
  
  private readonly personService = inject(PersonService);
  private readonly router = inject(Router);

  displayColumns: string[]=['nombre','actions'];
  dataSource = new MatTableDataSource<PersonModel>();
  
  @ViewChild(MatPaginator)
  paginator !: MatPaginator;

  ngOnInit(): void {
    this.getPersons();
    throw new Error('Method not implemented.');
  }

  getPersons():void{
    this.personService.getPersons()
      .subscribe({
        next: (v) => this.processPersonResponse(v),
        error: (e) => console.error(e),
        complete: () => console.info('complete') 
      });
  }

  edit(element:any){
    this.router.navigate(['dashboard/personDetails'],{
      queryParams: { element: JSON.stringify(element) },
      // skipLocationChange: true,
    });
  }

    processPersonResponse(resp:any){
      const dataUser: PersonModel[] = [];
      if (resp.metadata[0].code == "00"){
        let listUser = resp.data;
  
        listUser.forEach((element:PersonModel)=> {
          dataUser.push(element);
        });
        this.dataSource = new MatTableDataSource<PersonModel>(dataUser);
        this.dataSource.paginator = this.paginator;
      }
    }
}
