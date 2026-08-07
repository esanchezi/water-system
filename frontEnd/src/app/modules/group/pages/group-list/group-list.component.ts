import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { WaterGroupModel } from 'src/app/modules/shared/models/WaterUser.model';
import { GroupService } from 'src/app/modules/shared/services/group.service';
import { GroupNewComponent } from '../group-new/group-new.component';

@Component({
  selector: 'app-group-list',
  templateUrl: './group-list.component.html',
  styleUrls: ['./group-list.component.css']
})
export class GroupListComponent implements OnInit{
  private readonly router = inject(Router);
  private readonly groupService = inject(GroupService);
  private readonly dialog = inject(MatDialog);
  displayColumns: string[]=['grupoId','nombre','actions'];
  dataSource = new MatTableDataSource<WaterGroupModel>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  filters = {
    nombre: ''
  };

  ngOnInit(): void {
    this.getListGroup();
  }

  getListGroup():void{
    this.groupService.getListWaterGroup()
      .subscribe({
        next: (v) => this.processListWaterHouseResponse(v),
        error: (e) => console.error(e),
        complete: () => console.info('complete')
    });
  }

  edit(element: any) {
    this.router.navigate(['dashboard/groupDetails'], {
      queryParams: { element: JSON.stringify(element) },
    });
  }

  addGroup(): void {
    this.dialog.open(GroupNewComponent, { width: '450px' }).afterClosed().subscribe(result => {
      if (result) this.getListGroup();
    });
  }

  processListWaterHouseResponse(resp: any) {
      if (resp.metadata?.code === "00") {
        const data: WaterGroupModel[] = resp.data;
        this.dataSource = new MatTableDataSource<WaterGroupModel>(data);
        this.dataSource.paginator = this.paginator;

        this.dataSource.filterPredicate = (data: any, filter: string) => {
          const searchTerms = JSON.parse(filter);
          const matchNombre = !searchTerms.nombre || data.nombre?.toLowerCase().includes(searchTerms.nombre);
          return  matchNombre;
        };
      }
  }

  applyFilters(): void {
    this.dataSource.filter = JSON.stringify(this.filters);
  }

  applyNombreFilter(event: Event): void {
    this.filters.nombre = (event.target as HTMLInputElement).value
      .trim()
      .toLowerCase();

    this.applyFilters();
  }
}
