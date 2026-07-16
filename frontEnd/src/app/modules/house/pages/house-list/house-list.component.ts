import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { WaterHouseModel } from 'src/app/modules/shared/models/WaterUser.model';
import { HouseService } from 'src/app/modules/shared/services/house.service';
import { CatalogService } from 'src/app/modules/shared/services/catalog.service';
import { MatDialog } from '@angular/material/dialog';
import { HouseNewComponent } from '../house-new/house-new.component';

@Component({
  selector: 'app-house-list',
  templateUrl: './house-list.component.html',
  styleUrls: ['./house-list.component.css']
})
export class HouseListComponent implements OnInit {

  private readonly router        = inject(Router);
  private readonly houseService  = inject(HouseService);
  private readonly catalogService = inject(CatalogService);
  private readonly dialog        = inject(MatDialog);

  filters = { calleId: '', lado: '', nombre: '' };

  readonly lados = [
    { value: 'D', label: 'Derecho' },
    { value: 'I', label: 'Izquierdo' }
  ];

  calles$ = this.catalogService.getCatalogByIdResponse(15).pipe(
    map(resp => resp.data[0].options)
  );

  displayColumns: string[] = ['casaId', 'calleId', 'casaNo', 'lado', 'nombre', 'actions'];
  dataSource = new MatTableDataSource<WaterHouseModel>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.getListHouse();
  }

  getListHouse(): void {
    this.houseService.getListWaterHouse().subscribe({
      next: (resp) => this.processListWaterHouseResponse(resp),
      error: (e: any) => console.error(e)
    });
  }

  edit(element: WaterHouseModel): void {
    this.router.navigate(['dashboard/houseDetails'], {
      queryParams: { element: JSON.stringify(element) },
    });
  }

  processListWaterHouseResponse(resp: any): void {
    if (resp.metadata.code === '00') {
      const data: WaterHouseModel[] = resp.data;
      this.dataSource = new MatTableDataSource<WaterHouseModel>(data);
      this.dataSource.paginator = this.paginator;
      this.dataSource.filterPredicate = (row: any, filter: string) => {
        const searchTerms = JSON.parse(filter);
        const matchCalle  = !searchTerms.calleId || String(row.calleId) === String(searchTerms.calleId);
        const matchLado   = !searchTerms.lado    || row.lado === searchTerms.lado;
        const matchNombre = !searchTerms.nombre  || row.nombre?.toLowerCase().includes(searchTerms.nombre);
        return matchCalle && matchLado && matchNombre;
      };
    }
  }

  addHouse(): void {
    this.dialog.open(HouseNewComponent, {
      width: '800px',
    }).afterClosed().subscribe(result => {
      if (result) this.getListHouse();
    });
  }

  applyFilters(): void {
    this.dataSource.filter = JSON.stringify(this.filters);
  }

  applyCalleFilter(calleId: number | null): void {
    this.filters.calleId = calleId != null ? String(calleId) : '';
    this.applyFilters();
  }

  applyLadoFilter(lado: string | null): void {
    this.filters.lado = lado ?? '';
    this.applyFilters();
  }

  ladoLabel(lado: string): string {
    return this.lados.find(l => l.value === lado)?.label ?? lado ?? '';
  }

  applyNombreFilter(event: Event): void {
    this.filters.nombre = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.applyFilters();
  }
}
