import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { DeudorModel } from 'src/app/modules/shared/models/Deudor.model';
import { DeudorService } from 'src/app/modules/shared/services/deudor.service';

@Component({
  selector: 'app-deudor-list',
  templateUrl: './deudor-list.component.html',
  styleUrls: ['./deudor-list.component.css']
})
export class DeudorListComponent implements OnInit {

  private readonly deudorService = inject(DeudorService);

  displayColumns: string[] = [
    'calleNombre', 'casaNo', 'noUsuario', 'nombreCompleto', 'estatusComiteNombre',
    'montoCuotaAnio', 'montoCuotaPagado', 'montoCuotaPendiente', 'montoCargosPendiente', 'montoTotalPendiente'
  ];

  dataSource = new MatTableDataSource<DeudorModel>();

  readonly anioActual = new Date().getFullYear();
  anio: number = this.anioActual;
  // Rango razonable de años para el selector (desde que existe cuota_monto en la app).
  anios: number[] = Array.from({ length: 6 }, (_, i) => this.anioActual - i);

  calleFiltro = '';
  estatusComiteFiltro = '';
  // Se arma con los estatus que realmente vienen en los datos cargados (el
  // backend ya excluyó condonación/convenio/validar de las cuentas).
  estatusComiteOpciones: string[] = [];

  cargando = false;

  totalCuotaAnio = 0;
  totalCuotaPagado = 0;
  totalCuota = 0;
  totalCargos = 0;
  totalGeneral = 0;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.cargando = true;
    this.deudorService.getDeudores(this.anio).subscribe({
      next: (resp: any) => {
        this.cargando = false;
        if (resp.metadata?.[0]?.code === '00') {
          const data: DeudorModel[] = resp.data || [];
          // Ya viene ordenado por calle y N° de casa desde el backend, y ya
          // excluye a los usuarios en condonación/convenio/validar.
          this.dataSource = new MatTableDataSource<DeudorModel>(data);
          this.dataSource.paginator = this.paginator;
          this.dataSource.filterPredicate = (row: DeudorModel, filter: string) => {
            const f = JSON.parse(filter);
            const matchCalle = !f.calle || (row.calleNombre ?? '').toLowerCase().includes(f.calle);
            const matchEstatus = !f.estatus || row.estatusComiteNombre === f.estatus;
            return matchCalle && matchEstatus;
          };

          this.estatusComiteOpciones = [...new Set(
            data.map(d => d.estatusComiteNombre).filter((v): v is string => !!v)
          )].sort();

          this.applyFilters();
        }
      },
      error: (e: any) => {
        this.cargando = false;
        console.error('Error al cargar deudores', e);
      }
    });
  }

  applyCalleFilter(event: Event): void {
    this.calleFiltro = (event.target as HTMLInputElement).value;
    this.applyFilters();
  }

  applyEstatusComiteFilter(): void {
    this.applyFilters();
  }

  private applyFilters(): void {
    this.dataSource.filter = JSON.stringify({
      calle: this.calleFiltro.trim().toLowerCase(),
      estatus: this.estatusComiteFiltro
    });
    this.calcularTotales(this.dataSource.filteredData);
  }

  private calcularTotales(data: DeudorModel[]): void {
    this.totalCuotaAnio = data.reduce((acc, d) => acc + (Number(d.montoCuotaAnio) || 0), 0);
    this.totalCuotaPagado = data.reduce((acc, d) => acc + (Number(d.montoCuotaPagado) || 0), 0);
    this.totalCuota = data.reduce((acc, d) => acc + (Number(d.montoCuotaPendiente) || 0), 0);
    this.totalCargos = data.reduce((acc, d) => acc + (Number(d.montoCargosPendiente) || 0), 0);
    this.totalGeneral = data.reduce((acc, d) => acc + (Number(d.montoTotalPendiente) || 0), 0);
  }
}
