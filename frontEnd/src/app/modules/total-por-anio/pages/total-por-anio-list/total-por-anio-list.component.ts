import { Component, OnInit, inject } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { ConceptoResumenModel, TotalPorAnioModel } from 'src/app/modules/shared/models/TotalPorAnio.model';
import { TotalPorAnioService } from 'src/app/modules/shared/services/total-por-anio.service';

// Fila plana para la tabla: año, total del año, egresos, efectivo esperado,
// y un valor por cada concepto (llave = catalogoOpcionesId como string).
interface FilaPivote {
  anio: number;
  totalAnio: number;
  egresosAnio: number;
  efectivoEsperado: number;
  [conceptoId: string]: number;
}

@Component({
  selector: 'app-total-por-anio-list',
  templateUrl: './total-por-anio-list.component.html',
  styleUrls: ['./total-por-anio-list.component.css']
})
export class TotalPorAnioListComponent implements OnInit {

  private readonly totalPorAnioService = inject(TotalPorAnioService);

  conceptos: ConceptoResumenModel[] = [];
  displayColumns: string[] = [];
  dataSource = new MatTableDataSource<FilaPivote>();

  totalesPorConcepto: { [conceptoId: string]: number } = {};
  granTotal = 0;
  granEgresos = 0;

  cargando = false;

  ngOnInit(): void {
    this.load();
  }

  colId(c: ConceptoResumenModel): string {
    return String(c.catalogoOpcionesId);
  }

  load(): void {
    this.cargando = true;
    this.totalPorAnioService.getTotalesPorAnio().subscribe({
      next: (resp: any) => {
        this.cargando = false;
        if (resp.metadata?.[0]?.code !== '00') return;

        const data: TotalPorAnioModel | undefined = resp.data?.[0];
        if (!data) return;

        this.conceptos = data.conceptos || [];
        this.displayColumns = ['anio', ...this.conceptos.map(c => this.colId(c)), 'totalAnio', 'egresosAnio', 'efectivoEsperado'];

        const filas: FilaPivote[] = (data.filas || []).map(f => {
          const fila: FilaPivote = {
            anio: f.anio,
            totalAnio: f.totalAnio,
            egresosAnio: f.egresosAnio,
            efectivoEsperado: f.efectivoEsperado
          };
          this.conceptos.forEach(c => {
            fila[this.colId(c)] = f.montosPorConcepto?.[c.catalogoOpcionesId] ?? 0;
          });
          return fila;
        });

        this.dataSource = new MatTableDataSource<FilaPivote>(filas);
        this.totalesPorConcepto = data.totalesPorConcepto || {};
        this.granTotal = data.granTotal || 0;
        this.granEgresos = data.granEgresos || 0;
      },
      error: (e: any) => {
        this.cargando = false;
        console.error('Error al cargar totales por año', e);
      }
    });
  }
}
