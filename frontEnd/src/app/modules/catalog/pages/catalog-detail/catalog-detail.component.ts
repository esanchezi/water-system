import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { CatalogService } from '../../../shared/services/catalog.service';
import { CatalogModel, CatalogOptionModel } from 'src/app/modules/shared/models/Catalog.model';
import { CatalogOptionFormComponent } from '../../components/catalog-option-form/catalog-option-form.component';

@Component({
  selector: 'app-catalog-detail',
  templateUrl: './catalog-detail.component.html',
  styleUrls: ['./catalog-detail.component.css']
})
export class CatalogDetailComponent implements OnInit {

  private readonly route  = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);
  private readonly catalogService = inject(CatalogService);

  catalog!: CatalogModel;

  displayColumns: string[] = ['catalogoOpcionesId', 'nombre', 'descripcion', 'estatus', 'actions'];
  dataSource = new MatTableDataSource<CatalogOptionModel>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    const raw = this.route.snapshot.queryParams['element'];
    if (raw) {
      this.catalog = JSON.parse(raw);
      this.loadOptions();
    }
  }

  loadOptions(): void {
    this.catalogService.getById(this.catalog.catalogoId).subscribe({
      next: (cat) => {
        this.catalog = cat;
        this.dataSource = new MatTableDataSource<CatalogOptionModel>(cat.options ?? []);
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

  openAddOption(): void {
    this.dialog.open(CatalogOptionFormComponent, {
      width: '450px',
      data: { catalogoId: this.catalog.catalogoId, option: null }
    }).afterClosed().subscribe(result => {
      if (result) this.loadOptions();
    });
  }

  openEditOption(option: CatalogOptionModel): void {
    this.dialog.open(CatalogOptionFormComponent, {
      width: '450px',
      data: { catalogoId: this.catalog.catalogoId, option }
    }).afterClosed().subscribe(result => {
      if (result) this.loadOptions();
    });
  }

  deactivateOption(option: CatalogOptionModel): void {
    if (!confirm(`¿Desactivar la opción "${option.nombre}"?`)) return;
    this.catalogService.deactivateOption(this.catalog.catalogoId, option.catalogoOpcionesId).subscribe({
      next: () => this.loadOptions(),
      error: (e) => console.error(e)
    });
  }
}
