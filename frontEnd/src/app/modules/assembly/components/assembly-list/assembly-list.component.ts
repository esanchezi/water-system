import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { AssemblyModel } from 'src/app/modules/shared/models/Assembly.model';
import { AssemblyService } from 'src/app/modules/shared/services/assembly.service';

@Component({
  selector: 'app-assembly-list',
  templateUrl: './assembly-list.component.html',
  styleUrls: ['./assembly-list.component.css']
})
export class AssemblyListComponent implements OnInit{

  private readonly router = inject(Router);
  private readonly assemblyService = inject(AssemblyService);
  displayColumns: string[]=['dateS','proposito','actions'];
  dataSource = new MatTableDataSource<AssemblyModel>();

  @ViewChild(MatPaginator)
  paginator !: MatPaginator;

  ngOnInit(): void {
    this.getAssembly();
    throw new Error('Method not implemented.');
  }

  getAssembly():void{
    this.assemblyService.getAssembly()
      .subscribe({
        next: (v) => this.processAssemblyResponse(v),
        error: (e) => console.error(e),
        complete: () => console.info('complete') 
      });
  }

  processAssemblyResponse(resp:any){
    const dataAssembly: AssemblyModel[] = [];
    if (resp.metadata[0].code == "00"){
      let listAssembly = resp.data;
    
      listAssembly.forEach((element:AssemblyModel)=> {
        dataAssembly.push(element);
      });
      
      this.dataSource = new MatTableDataSource<AssemblyModel>(dataAssembly);
      this.dataSource.paginator = this.paginator;
    }
  }

  edit(element:any){
    this.router.navigate(['dashboard/assemblyDetails'],{
      queryParams: { element: JSON.stringify(element) },
      // skipLocationChange: true,
    });
  }

}
