import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EgresoLineaFlatModel, EgresoNodoModel } from 'src/app/modules/shared/models/EgresoNodo.model';

// Componente recursivo: renderiza una lista de nodos del árbol de Egresos
// (Categoría → Subconcepto → Año → Mes → Proveedor). Si un nodo tiene hijos
// se dibuja otro acordeón anidado con este mismo componente; si es hoja, se
// listan las líneas individuales con su acción de desactivar el vale.
@Component({
  selector: 'app-egreso-nodo-lista',
  templateUrl: './egreso-nodo-lista.component.html',
  styleUrls: ['./egreso-nodo-lista.component.css']
})
export class EgresoNodoListaComponent {
  @Input() nodos: EgresoNodoModel[] = [];
  @Output() desactivar = new EventEmitter<EgresoLineaFlatModel>();

  onDesactivar(fila: EgresoLineaFlatModel): void {
    this.desactivar.emit(fila);
  }
}
