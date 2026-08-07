import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { WaterHouseModel } from 'src/app/modules/shared/models/WaterUser.model';
import { CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
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

  filters = { calleId: '', seccionId: '', lado: '', nombre: '' };
  // Para que los <mat-select> de Sección/Calle reflejen visualmente el
  // reset cuando se vuelve a elegir "Todas" en Sección (sin esto, el
  // mat-select se queda mostrando la calle elegida antes aunque el filtro
  // interno sí se haya limpiado).
  seccionSeleccionada: number | null = null;
  calleSeleccionada: number | null = null;

  readonly lados = [
    { value: 'D', label: 'Derecho' },
    { value: 'I', label: 'Izquierdo' }
  ];

  secciones: CatalogOptionModel[] = [];
  private todasLasCalles: CatalogOptionModel[] = [];
  callesDeSeccion: CatalogOptionModel[] = [];
  // Para poder filtrar la tabla por sección aunque no se elija una calle
  // en particular (calleId -> zonaId de esa calle).
  private zonaPorCalleId = new Map<number, number>();

  displayColumns: string[] = ['casaId', 'calleId', 'casaNo', 'lado', 'nombre', 'actions'];
  dataSource = new MatTableDataSource<WaterHouseModel>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.getListHouse();
    this.cargarSeccionesYCalles();
  }

  private cargarSeccionesYCalles(): void {
    this.catalogService.getOptionsByClave('SECCIONES_COLONIA').subscribe({
      next: (opts) => this.secciones = [...opts].sort((a, b) => a.nombre.localeCompare(b.nombre)),
      error: (e: any) => console.error(e)
    });
    this.catalogService.getOptions(15).subscribe({
      next: (opts) => {
        this.todasLasCalles = [...opts].sort((a, b) => a.nombre.localeCompare(b.nombre));
        this.callesDeSeccion = this.todasLasCalles;
        this.zonaPorCalleId = new Map(
          opts.filter(c => c.zonaId != null).map(c => [c.catalogoOpcionesId, c.zonaId as number])
        );
      },
      error: (e: any) => console.error(e)
    });
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
        const matchCalle   = !searchTerms.calleId   || String(row.calleId) === String(searchTerms.calleId);
        const matchSeccion = !searchTerms.seccionId ||
          String(this.zonaPorCalleId.get(row.calleId)) === String(searchTerms.seccionId);
        const matchLado    = !searchTerms.lado    || row.lado === searchTerms.lado;
        const matchNombre  = !searchTerms.nombre  || row.nombre?.toLowerCase().includes(searchTerms.nombre);
        return matchCalle && matchSeccion && matchLado && matchNombre;
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

  // Al elegir Sección se filtra el dropdown de Calle a solo las de esa
  // sección, y también se filtra la tabla por sección (aunque no se elija
  // una calle en particular) para poder ver "todas las casas de La Barca".
  applySeccionFilter(seccionId: number | null): void {
    this.seccionSeleccionada = seccionId;
    this.calleSeleccionada = null;
    this.filters.seccionId = seccionId != null ? String(seccionId) : '';
    this.filters.calleId = '';
    this.callesDeSeccion = seccionId != null
      ? this.todasLasCalles.filter(c => c.zonaId === seccionId)
      : this.todasLasCalles;
    this.applyFilters();
  }

  applyCalleFilter(calleId: number | null): void {
    this.calleSeleccionada = calleId;
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
