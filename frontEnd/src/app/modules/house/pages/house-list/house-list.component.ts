import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { WaterHouseModel } from 'src/app/modules/shared/models/WaterUser.model';
import { HouseService } from 'src/app/modules/shared/services/house.service';
import { HouseFormComponent } from '../../components/house-form/house-form.component';
import { MatDialog } from '@angular/material/dialog';
import { HouseNewComponent } from '../house-new/house-new.component';

@Component({
  selector: 'app-house-list',
  templateUrl: './house-list.component.html',
  styleUrls: ['./house-list.component.css']
})
export class HouseListComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly houseService = inject(HouseService);

  private readonly dialog = inject(MatDialog); 

  filters = {
    zona: '',
    nombre: ''
  };

  displayColumns: string[] = ['casaId', 'seccionId', 'casaNo', 'nombre', 'actions'];
  dataSource = new MatTableDataSource<WaterHouseModel>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.getListHouse();
  }

  getListHouse(): void {
    this.houseService.getListWaterHouse().subscribe({
      next: (resp) => this.processListWaterHouseResponse(resp),
      error: (e) => console.error(e),
      complete: () => console.info('complete')
    });
  }

  edit(element: any) {
    this.router.navigate(['dashboard/houseDetails'], {
      queryParams: { element: JSON.stringify(element) },
    });
  }

  processListWaterHouseResponse(resp: any) {
    if (resp.metadata.code === "00") {
      const data: WaterHouseModel[] = resp.data;
      this.dataSource = new MatTableDataSource<WaterHouseModel>(data);
      this.dataSource.paginator = this.paginator;

      this.dataSource.filterPredicate = (data: any, filter: string) => {
        const searchTerms = JSON.parse(filter);
        const matchSeccion = !searchTerms.zona || data.zona?.toLowerCase().includes(searchTerms.zona);
        const matchNombre = !searchTerms.nombre || data.nombre?.toLowerCase().includes(searchTerms.nombre);
        return matchSeccion && matchNombre;
      };
    }
  }

  addHouse() {
    console.log("addHouse");
    this.dialog.open(HouseNewComponent, {
      width: '800px',
    }).afterClosed().subscribe(result => {
      if (result) {
        this.getListHouse();
      }
    });
  }

  applyFilters(): void {
    this.dataSource.filter = JSON.stringify(this.filters);
  }

  applyZonaFilter(event: Event): void {
    this.filters.zona = (event.target as HTMLInputElement).value
      .trim()
      .toLowerCase();

    this.applyFilters();
  }

  applyNombreFilter(event: Event): void {
    this.filters.nombre = (event.target as HTMLInputElement).value
      .trim()
      .toLowerCase();

    console.log (this.filters.nombre);

    this.applyFilters();
  }
}
