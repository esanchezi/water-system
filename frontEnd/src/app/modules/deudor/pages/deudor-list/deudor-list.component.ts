import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { DeudorModel } from 'src/app/modules/shared/models/Deudor.model';
import { DeudorService } from 'src/app/modules/shared/services/deudor.service';
import { PreregistroUsuarioModel } from 'src/app/modules/shared/models/PreregistroUsuario.model';
import { PreregistroUsuarioService } from 'src/app/modules/shared/services/preregistro-usuario.service';

@Component({
  selector: 'app-deudor-list',
  templateUrl: './deudor-list.component.html',
  styleUrls: ['./deudor-list.component.css']
})
export class DeudorListComponent implements OnInit {

  private readonly deudorService = inject(DeudorService);
  private readonly preregistroService = inject(PreregistroUsuarioService);
  private readonly router = inject(Router);

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
  casaFiltro = '';
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

  // Gente en preregistro (todavía no es usuario formal) con deuda
  // aproximada capturada -- se muestra aparte porque no tiene el mismo
  // desglose por año que un deudor real, solo un estimado manual.
  listPreregistroDeuda: PreregistroUsuarioModel[] = [];
  cargandoPreregistro = false;
  totalPreregistroAportaciones = 0;
  totalPreregistroMultas = 0;
  totalPreregistroGeneral = 0;

  ngOnInit(): void {
    this.load();
  }

  getPreregistroConDeuda(): void {
    if (this.listPreregistroDeuda.length > 0 || this.cargandoPreregistro) return;
    this.cargandoPreregistro = true;
    this.preregistroService.getConDeuda().subscribe({
      next: (resp: any) => {
        this.cargandoPreregistro = false;
        if (resp.metadata?.[0]?.code === '00') {
          const data: PreregistroUsuarioModel[] = resp.data || [];
          this.listPreregistroDeuda = data;
          this.totalPreregistroAportaciones = data.reduce((acc, p) => acc + (Number(p.deudaAportaciones) || 0), 0);
          this.totalPreregistroMultas = data.reduce((acc, p) => acc + (Number(p.deudaMultasRecargos) || 0), 0);
          this.totalPreregistroGeneral = this.totalPreregistroAportaciones + this.totalPreregistroMultas;
        }
      },
      error: (e: any) => {
        this.cargandoPreregistro = false;
        console.error('Error al cargar preregistro con deuda', e);
      }
    });
  }

  totalDeudaPreregistro(p: PreregistroUsuarioModel): number {
    return (Number(p.deudaAportaciones) || 0) + (Number(p.deudaMultasRecargos) || 0);
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
            const matchCasa = !f.casa || String(row.casaNo ?? '').includes(f.casa);
            const matchEstatus = !f.estatus || row.estatusComiteNombre === f.estatus;
            return matchCalle && matchCasa && matchEstatus;
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

  // Lleva al detalle del usuario (misma pantalla que usa el módulo de
  // usuarios). Solo se necesita usuarioId -- details-user vuelve a pedir
  // todos los datos completos al backend con ese id.
  verDetalleUsuario(d: DeudorModel): void {
    this.router.navigate(['dashboard/detailsUser'], {
      queryParams: { element: JSON.stringify({ usuarioId: d.aguaUsuarioId }) }
    });
  }

  applyCalleFilter(event: Event): void {
    this.calleFiltro = (event.target as HTMLInputElement).value;
    this.applyFilters();
  }

  applyCasaFilter(event: Event): void {
    this.casaFiltro = (event.target as HTMLInputElement).value;
    this.applyFilters();
  }

  applyEstatusComiteFilter(): void {
    this.applyFilters();
  }

  private applyFilters(): void {
    this.dataSource.filter = JSON.stringify({
      calle: this.calleFiltro.trim().toLowerCase(),
      casa: this.casaFiltro.trim(),
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
