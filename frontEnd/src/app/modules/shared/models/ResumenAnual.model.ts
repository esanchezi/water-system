// Reproduce la pestaña "Resumen" de la hoja de cálculo: por año, un
// resumen de caja y, dentro de ese año, el desglose mes a mes y por
// categoría de egreso.

export interface ResumenAnualMesModel {
  mes: number;
  mesNombre: string;
  totalMes: number;
  recibosCaja: number;
  efectivo: number;
  egresos: number;
}

export interface ResumenAnualCategoriaMesModel {
  mes: number;
  mesNombre: string;
  luz: number;
  mantenimiento: number;
}

export interface ResumenAnualFilaModel {
  anio: number;
  inicial: number;
  recibosCaja: number;
  efectivo: number;
  egresos: number;
  saldoCaja: number;
  meses: ResumenAnualMesModel[];
  categorias: ResumenAnualCategoriaMesModel[];
}

export interface ResumenAnualModel {
  filas: ResumenAnualFilaModel[];
}
