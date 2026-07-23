import { Component, OnInit, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarRef, SimpleSnackBar } from '@angular/material/snack-bar';
import { WaterEgresoGastoModel } from 'src/app/modules/shared/models/WaterEgresoGasto.model';
import { WaterEgresoService } from 'src/app/modules/shared/services/water-egreso.service';
import { NewGastoComponent } from '../../components/new-gasto/new-gasto.component';
import { EmitirValeComponent } from '../../components/emitir-vale/emitir-vale.component';

interface PersonaGrupo {
  key: string;
  nombre: string;
  total: number;
  gastos: WaterEgresoGastoModel[];
}

interface ConceptoGrupo {
  conceptoId: number;
  conceptoNombre: string;
  total: number;
  personas: PersonaGrupo[];
}

// Pantalla "amigable" de fin de mes: los gastos se van capturando uno por
// uno durante el mes (sin vale todavía) y aquí se revisan agrupados por
// categoría y, dentro de cada categoría, por persona/proveedor -- con el
// total en el encabezado de cada acordeón. Al final se seleccionan los
// gastos a incluir y se emite un solo vale que los junta.
@Component({
  selector: 'app-egreso-gastos',
  templateUrl: './egreso-gastos.component.html',
  styleUrls: ['./egreso-gastos.component.css']
})
export class EgresoGastosComponent implements OnInit {

  private readonly egresoService = inject(WaterEgresoService);
  private readonly snackBar = inject(MatSnackBar);
  public dialog = inject(MatDialog);

  pendientes: WaterEgresoGastoModel[] = [];
  grupos: ConceptoGrupo[] = [];
  seleccionados = new Set<number>();

  ngOnInit(): void {
    this.getPendientes();
  }

  getPendientes(): void {
    this.egresoService.getPendientes().subscribe({
      next: (resp: any) => this.processResponse(resp),
      error: (e: any) => console.error(e)
    });
  }

  private processResponse(resp: any): void {
    if (resp.metadata?.[0]?.code !== '00') return;
    this.pendientes = resp.data || [];
    this.seleccionados.clear();
    this.grupos = this.agrupar(this.pendientes);
  }

  private agrupar(gastos: WaterEgresoGastoModel[]): ConceptoGrupo[] {
    const conceptosMap = new Map<number, ConceptoGrupo>();

    gastos.forEach(g => {
      let concepto = conceptosMap.get(g.conceptoId);
      if (!concepto) {
        concepto = { conceptoId: g.conceptoId, conceptoNombre: g.conceptoNombre || 'Sin categoría', total: 0, personas: [] };
        conceptosMap.set(g.conceptoId, concepto);
      }
      concepto.total += Number(g.monto) || 0;

      // Se agrupa por proveedor (a quién se le pagó/compró), no por quién
      // gestionó el pago: casi todo lo gestiona la misma persona, así que
      // agrupar por ahí no ayudaría a revisar el gasto.
      const proveedorLabel = g.proveedor || g.personaNombre || 'Sin proveedor';
      const personaKey = `prov:${proveedorLabel}`;
      let persona = concepto.personas.find(p => p.key === personaKey);
      if (!persona) {
        persona = { key: personaKey, nombre: proveedorLabel, total: 0, gastos: [] };
        concepto.personas.push(persona);
      }
      persona.total += Number(g.monto) || 0;
      persona.gastos.push(g);
    });

    const grupos = Array.from(conceptosMap.values());
    grupos.forEach(concepto => {
      concepto.personas.sort((a, b) => a.nombre.localeCompare(b.nombre));
      // Los gastos ya vienen ordenados por fecha desde el backend, pero se
      // ordenan también aquí por si acaso (defensivo, no depender solo del
      // orden con el que lleguen).
      concepto.personas.forEach(persona => {
        persona.gastos.sort((a, b) => (a.fechaPago || '').localeCompare(b.fechaPago || ''));
      });
    });

    return grupos.sort((a, b) => a.conceptoNombre.localeCompare(b.conceptoNombre));
  }

  get totalPendientes(): number {
    return this.pendientes.reduce((acc, g) => acc + (Number(g.monto) || 0), 0);
  }

  get totalSeleccionado(): number {
    return this.pendientes
      .filter(g => g.aguaEgresosId != null && this.seleccionados.has(g.aguaEgresosId))
      .reduce((acc, g) => acc + (Number(g.monto) || 0), 0);
  }

  isSelected(gasto: WaterEgresoGastoModel): boolean {
    return gasto.aguaEgresosId != null && this.seleccionados.has(gasto.aguaEgresosId);
  }

  toggleGasto(gasto: WaterEgresoGastoModel, checked: boolean): void {
    if (gasto.aguaEgresosId == null) return;
    if (checked) {
      this.seleccionados.add(gasto.aguaEgresosId);
    } else {
      this.seleccionados.delete(gasto.aguaEgresosId);
    }
  }

  isPersonaFullySelected(persona: PersonaGrupo): boolean {
    return persona.gastos.every(g => this.isSelected(g));
  }

  togglePersona(persona: PersonaGrupo, checked: boolean): void {
    persona.gastos.forEach(g => this.toggleGasto(g, checked));
  }

  isConceptoFullySelected(concepto: ConceptoGrupo): boolean {
    return concepto.personas.every(p => this.isPersonaFullySelected(p));
  }

  toggleConcepto(concepto: ConceptoGrupo, checked: boolean): void {
    concepto.personas.forEach(p => this.togglePersona(p, checked));
  }

  openNewGastoDialog(): void {
    const dialogRef = this.dialog.open(NewGastoComponent, { width: '700px' });
    dialogRef.afterClosed().subscribe((result: any) => {
      if (result === 1) {
        this.openSnackBar('Gasto registrado', 'Éxito');
        this.getPendientes();
      } else if (result === 2) {
        this.openSnackBar('Error al guardar el gasto', 'Error');
      }
    });
  }

  openEditarGastoDialog(gasto: WaterEgresoGastoModel): void {
    const dialogRef = this.dialog.open(NewGastoComponent, {
      width: '700px',
      data: { gasto }
    });
    dialogRef.afterClosed().subscribe((result: any) => {
      if (result === 1) {
        this.openSnackBar('Gasto actualizado', 'Éxito');
        this.getPendientes();
      } else if (result === 2) {
        this.openSnackBar('Error al actualizar el gasto', 'Error');
      }
    });
  }

  openEmitirValeDialog(): void {
    if (this.seleccionados.size === 0) return;

    const dialogRef = this.dialog.open(EmitirValeComponent, {
      width: '700px',
      data: {
        gastoIds: Array.from(this.seleccionados),
        totalSeleccionado: this.totalSeleccionado
      }
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result === 1) {
        this.openSnackBar('Vale emitido', 'Éxito');
        this.getPendientes();
      } else if (result === 2) {
        this.openSnackBar('Error al emitir el vale', 'Error');
      }
    });
  }

  openSnackBar(message: string, action: string): MatSnackBarRef<SimpleSnackBar> {
    return this.snackBar.open(message, action, { duration: 2000 });
  }
}
