import { Component, OnInit, inject } from '@angular/core';
import { ResumenAnualFilaModel } from 'src/app/modules/shared/models/ResumenAnual.model';
import { ResumenAnualService } from 'src/app/modules/shared/services/resumen-anual.service';

// Reproduce la pestaña "Resumen" de la hoja de cálculo, como acordeón:
// nivel 1 = año; nivel 2 = resumen de caja del año (inicial/recibos caja/
// efectivo/egresos/saldo); nivel 3 = desglose mes a mes y por categoría de
// egreso (Luz / Mantenimiento). Pantalla nueva, separada de "Totales por
// año" (que sigue igual, solo considera efectivo real).
@Component({
  selector: 'app-resumen-anual-list',
  templateUrl: './resumen-anual-list.component.html',
  styleUrls: ['./resumen-anual-list.component.css']
})
export class ResumenAnualListComponent implements OnInit {

  private readonly resumenAnualService = inject(ResumenAnualService);

  filas: ResumenAnualFilaModel[] = [];
  cargando = false;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.cargando = true;
    this.resumenAnualService.getResumenAnual().subscribe({
      next: (resp: any) => {
        this.cargando = false;
        if (resp.metadata?.[0]?.code !== '00') return;
        this.filas = resp.data?.[0]?.filas || [];
      },
      error: (e: any) => {
        this.cargando = false;
        console.error('Error al cargar el resumen anual', e);
      }
    });
  }

  // Totales de pie de tabla para la tabla "por categoría" (el backend solo
  // manda el desglose mensual; el total anual ya viene en fila.egresos pero
  // no separado por Luz/Mantenimiento).
  sumaLuz(fila: ResumenAnualFilaModel): number {
    return fila.categorias.reduce((acc, c) => acc + (c.luz || 0), 0);
  }

  sumaMantenimiento(fila: ResumenAnualFilaModel): number {
    return fila.categorias.reduce((acc, c) => acc + (c.mantenimiento || 0), 0);
  }
}
