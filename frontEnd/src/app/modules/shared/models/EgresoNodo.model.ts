// Fila plana (una línea de un vale, ya con su fecha/proveedor propios y una
// referencia a la cabecera a la que pertenece) usada para armar el árbol del
// acordeón histórico de Egresos.
export interface EgresoLineaFlatModel {
  lineaId?: number;
  valeId: number;
  noFolio?: string;
  fecha: string;
  fechaStr: string;
  // Fecha del vale (cabecera), usada SOLO para decidir en qué año/mes cae
  // en el acordeón. Puede diferir de `fecha` (la propia de la línea, ej. la
  // fecha de lectura de un recibo de CFE) cuando el pago se hizo después.
  fechaAgrupacion: string;
  descripcion?: string;
  monto: number;
  proveedor: string;
}

// Nodo del árbol Categoría → (Subconcepto) → Año → Mes → Proveedor. Un nodo
// es una rama (tiene children) o una hoja (tiene filas), nunca ambos.
export interface EgresoNodoModel {
  label: string;
  total: number;
  children?: EgresoNodoModel[];
  filas?: EgresoLineaFlatModel[];
}
