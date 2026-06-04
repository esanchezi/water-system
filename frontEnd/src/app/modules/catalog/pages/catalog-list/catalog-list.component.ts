import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { CatalogService } from '../../../shared/services/catalog.service';
import { CatalogModel } from 'src/app/modules/shared/models/Catalog.model';
import { CatalogFormComponent } from '../../components/catalog-form/catalog-form.component';

@Component({
  selector: 'app-catalog-list',
  templateUrl: './catalog-list.component.html',
  styleUrls: ['./catalog-list.component.css']
})
export class CatalogListComponent implements OnInit {

  private readonly router  = inject(Router);
  private readonly dialog  = inject(MatDialog);
  private readonly catalogService = inject(CatalogService);

  displayColumns: string[] = ['catalogoId', 'nombre', 'descripcion', 'totalOpciones', 'estatus', 'actions'];
  dataSource = new MatTableDataSource<CatalogModel>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.catalogService.getAll().subscribe({
      next: (data) => {
        this.dataSource = new MatTableDataSource<CatalogModel>(data);
        this.dataSource.paginator = this.paginator;
        this.dataSource.filterPredicate = (row, filter) =>
          `${row.nombre} ${row.descripcion}`.toLowerCase().includes(filter);
      },
      error: (e) => console.error(e)
    });
  }

  applyFilter(event: Event): void {
    this.dataSource.filter = (event.target as HTMLInputElement).value.trim().toLowerCase();
  }

  openCreate(): void {
    this.dialog.open(CatalogFormComponent, {
      width: '500px',
      data: null
    }).afterClosed().subscribe(result => {
      if (result) this.load();
    });
  }

  openEdit(catalog: CatalogModel): void {
    this.dialog.open(CatalogFormComponent, {
      width: '500px',
      data: catalog
    }).afterClosed().subscribe(result => {
      if (result) this.load();
    });
  }

  goToDetail(catalog: CatalogModel): void {
    this.router.navigate(['dashboard/catalogDetail'], {
      queryParams: { element: JSON.stringify(catalog) }
    });
  }

  deactivate(catalog: CatalogModel): void {
    if (!confirm(`¿Desactivar el catálogo "${catalog.nombre}"?`)) return;
    this.catalogService.deactivate(catalog.catalogoId).subscribe({
      next: () => this.load(),
      error: (e) => console.error(e)
    });
  }
}
