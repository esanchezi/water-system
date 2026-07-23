import { Component, OnInit, inject } from '@angular/core';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import Swal from 'sweetalert2';
import { WaterEgresoModel } from 'src/app/modules/shared/models/WaterEgreso.model';
import { EgresoLineaFlatModel, EgresoNodoModel } from 'src/app/modules/shared/models/EgresoNodo.model';
import { WaterEgresoService } from 'src/app/modules/shared/services/water-egreso.service';

const NOMBRES_MES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
];

// Conceptos que caen bajo el paraguas de "Mantenimiento" (todo lo que no sea
// "Luz"): Nomina, Obras, Cortes, Papeleria, Viaticos, Varios y el propio
// "Mantenimiento" genérico cuando no se especifica un subconcepto.
function esPrincipalLuz(conceptoNombre: string): boolean {
  return (conceptoNombre || '').trim().toLowerCase() === 'luz';
}

// Los montos armados a mano en HTML (fuera de un pipe de Angular) necesitan
// su propio formateo con separador de miles; `toFixed(2)` no lo agrega.
function formatMonto(monto: number): string {
  return Number(monto).toLocaleString('es-MX', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// Vista histórica de todos los vales ya emitidos, agrupada para revisar
// rápido: Categoría principal (Luz / Mantenimiento) → Subconcepto (solo
// Mantenimiento) → Año → Mes → Proveedor, con el total en cada encabezado
// de acordeón. Reemplaza la tabla plana anterior.
@Component({
  selector: 'app-egreso-list',
  templateUrl: './egreso-list.component.html',
  styleUrls: ['./egreso-list.component.css']
})
export class EgresoListComponent implements OnInit {

  private readonly egresoService = inject(WaterEgresoService);
  private readonly snackBar = inject(MatSnackBar);

  vales: WaterEgresoModel[] = [];
  arbol: EgresoNodoModel[] = [];
  totalGeneral = 0;

  ngOnInit(): void {
    this.getEgresos();
  }

  getEgresos(): void {
    this.egresoService.getAll().subscribe({
      next: (resp: any) => this.processResponse(resp),
      error: (e: any) => console.error(e)
    });
  }

  private processResponse(resp: any): void {
    if (resp.metadata?.[0]?.code !== '00') return;
    this.vales = resp.data || [];
    this.arbol = this.construirArbol(this.vales);
    this.totalGeneral = this.arbol.reduce((acc, n) => acc + n.total, 0);
  }

  private sum(filas: EgresoLineaFlatModel[]): number {
    return filas.reduce((acc, f) => acc + (Number(f.monto) || 0), 0);
  }

  private aplanar(vales: WaterEgresoModel[]): { fila: EgresoLineaFlatModel; conceptoNombre: string }[] {
    const resultado: { fila: EgresoLineaFlatModel; conceptoNombre: string }[] = [];
    vales.forEach(vale => {
      if (vale.lineas && vale.lineas.length > 0) {
        vale.lineas.forEach(linea => {
          const proveedor = linea.proveedor || linea.personaNombre || vale.proveedor || vale.personaNombre || 'Sin proveedor';
          const fecha = linea.fechaPago || vale.fechaPago;
          const fechaStr = linea.fechaPagoStr || vale.fechaPagoStr || '';
          resultado.push({
            conceptoNombre: linea.conceptoNombre || 'Sin categoría',
            fila: {
              lineaId: linea.aguaEgresosId,
              valeId: vale.aguaEgresosId!,
              noFolio: vale.noFolio,
              fecha,
              fechaStr,
              // El año/mes del acordeón siempre se decide por la fecha del
              // vale (cuándo se pagó), no por la de la línea (que puede ser
              // la fecha de lectura de un recibo, de un mes distinto).
              fechaAgrupacion: vale.fechaPago,
              descripcion: linea.descripcion || vale.descripcion,
              monto: Number(linea.monto) || 0,
              proveedor
            }
          });
        });
      } else {
        // Vale histórico (previo al diseño de líneas por categoría): la
        // cabecera trae su propio concepto y monto, no tiene hijos.
        const proveedor = vale.proveedor || vale.personaNombre || 'Sin proveedor';
        resultado.push({
          conceptoNombre: vale.conceptoNombre || 'Sin categoría',
          fila: {
            valeId: vale.aguaEgresosId!,
            noFolio: vale.noFolio,
            fecha: vale.fechaPago,
            fechaStr: vale.fechaPagoStr || '',
            fechaAgrupacion: vale.fechaPago,
            descripcion: vale.descripcion,
            monto: Number(vale.monto) || 0,
            proveedor
          }
        });
      }
    });
    return resultado;
  }

  private construirArbol(vales: WaterEgresoModel[]): EgresoNodoModel[] {
    const filasConConcepto = this.aplanar(vales);

    const luz: EgresoLineaFlatModel[] = [];
    const mantenimientoPorSub = new Map<string, EgresoLineaFlatModel[]>();

    filasConConcepto.forEach(({ fila, conceptoNombre }) => {
      if (esPrincipalLuz(conceptoNombre)) {
        luz.push(fila);
      } else {
        if (!mantenimientoPorSub.has(conceptoNombre)) {
          mantenimientoPorSub.set(conceptoNombre, []);
        }
        mantenimientoPorSub.get(conceptoNombre)!.push(fila);
      }
    });

    const nodos: EgresoNodoModel[] = [];

    if (luz.length > 0) {
      nodos.push({ label: 'Luz', total: this.sum(luz), children: this.agruparPorAnioMesProveedor(luz) });
    }

    if (mantenimientoPorSub.size > 0) {
      const subNodos: EgresoNodoModel[] = Array.from(mantenimientoPorSub.entries())
        .map(([nombre, filas]) => ({
          label: nombre,
          total: this.sum(filas),
          children: this.agruparPorAnioMesProveedor(filas)
        }))
        .sort((a, b) => a.label.localeCompare(b.label));

      const totalMantenimiento = subNodos.reduce((acc, n) => acc + n.total, 0);
      nodos.push({ label: 'Mantenimiento', total: totalMantenimiento, children: subNodos });
    }

    return nodos;
  }

  // OJO: nunca usar `new Date(f.fecha)` para sacar año/mes. Un string tipo
  // "2024-02-01" se interpreta como medianoche UTC, y al convertirlo a la
  // zona horaria local (México, detrás de UTC) se puede recorrer al día
  // anterior -> el mes sale mal (un 1° de febrero se lee como enero). Por
  // eso se parte el texto directo, sin pasar por Date.
  private anioMes(fecha: string): { anio: number; mes: number } {
    const [anioStr, mesStr] = fecha.split('-');
    return { anio: Number(anioStr), mes: Number(mesStr) - 1 };
  }

  private agruparPorAnioMesProveedor(filas: EgresoLineaFlatModel[]): EgresoNodoModel[] {
    const porAnio = new Map<number, EgresoLineaFlatModel[]>();
    filas.forEach(f => {
      const anio = this.anioMes(f.fechaAgrupacion).anio;
      if (!porAnio.has(anio)) porAnio.set(anio, []);
      porAnio.get(anio)!.push(f);
    });

    return Array.from(porAnio.keys())
      .sort((a, b) => b - a)
      .map(anio => {
        const filasAnio = porAnio.get(anio)!;
        const porMes = new Map<number, EgresoLineaFlatModel[]>();
        filasAnio.forEach(f => {
          const mes = this.anioMes(f.fechaAgrupacion).mes;
          if (!porMes.has(mes)) porMes.set(mes, []);
          porMes.get(mes)!.push(f);
        });

        const nodosMes: EgresoNodoModel[] = Array.from(porMes.keys())
          .sort((a, b) => a - b)
          .map(mes => {
            const filasMes = porMes.get(mes)!;
            const porProveedor = new Map<string, EgresoLineaFlatModel[]>();
            filasMes.forEach(f => {
              if (!porProveedor.has(f.proveedor)) porProveedor.set(f.proveedor, []);
              porProveedor.get(f.proveedor)!.push(f);
            });

            const nodosProveedor: EgresoNodoModel[] = Array.from(porProveedor.entries())
              .map(([proveedor, filasProv]) => ({ label: proveedor, total: this.sum(filasProv), filas: filasProv }))
              .sort((a, b) => a.label.localeCompare(b.label));

            return { label: NOMBRES_MES[mes], total: this.sum(filasMes), children: nodosProveedor };
          });

        return { label: String(anio), total: this.sum(filasAnio), children: nodosMes };
      });
  }

  onDesactivar(fila: EgresoLineaFlatModel): void {
    const vale = this.vales.find(v => v.aguaEgresosId === fila.valeId);
    const lineasHtml = (vale?.lineas || [])
      .map(l => `<tr><td style="text-align:left">${l.conceptoNombre || ''}</td><td style="text-align:right">$${formatMonto(l.monto)}</td></tr>`)
      .join('');
    const advertencia = (vale?.lineas || []).length > 1
      ? '<p style="color:#b00020">Este vale tiene varias categorías: se desactivarán todas juntas.</p>'
      : '';

    Swal.fire({
      title: `¿Desactivar el vale ${vale?.noFolio || fila.valeId} completo?`,
      html: `${advertencia}<table style="width:100%"><thead><tr><th style="text-align:left">Categoría</th><th style="text-align:right">Monto</th></tr></thead><tbody>${lineasHtml}</tbody></table>`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, desactivar',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (!result.isConfirmed) return;
      this.egresoService.deactivate(fila.valeId).subscribe({
        next: () => {
          this.openSnackBar('Vale desactivado', 'Éxito');
          this.getEgresos();
        },
        error: () => this.openSnackBar('Error al desactivar el vale', 'Error')
      });
    });
  }

  openSnackBar(message: string, action: string): MatSnackBarRef<SimpleSnackBar> {
    return this.snackBar.open(message, action, { duration: 2000 });
  }
}
