// Reporte pivote: columnas = conceptos de cobro, filas = años.
export interface ConceptoResumenModel {
  catalogoOpcionesId: number;
  nombre: string;
}

export interface TotalPorAnioFilaModel {
  anio: number;
  montosPorConcepto: { [conceptoId: number]: number };
  totalAnio: number;
  egresosAnio: number;
  efectivoEsperado: number;
}

export interface TotalPorAnioModel {
  conceptos: ConceptoResumenModel[];
  filas: TotalPorAnioFilaModel[];
  totalesPorConcepto: { [conceptoId: number]: number };
  granTotal: number;
  granEgresos: number;
}
