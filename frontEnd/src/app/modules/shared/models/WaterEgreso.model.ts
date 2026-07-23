export interface WaterEgresoLineaModel {
  aguaEgresosId?: number;
  conceptoId: number;
  conceptoNombre?: string;
  monto: number;
  descripcion?: string;
  fechaPago?: string;
  fechaPagoStr?: string;
  proveedor?: string;
  personaId?: number;
  personaNombre?: string;

  // Folio propio de la línea (compra/recibo individual), distinto del folio
  // del vale que la agrupa. Es el dato clave para saber qué comprobante
  // físico corresponde a esta línea.
  noFolio?: string;

  // Desglose interno (dato histórico), cuando esta línea es a su vez la
  // suma de varias sub-compras. Solo para mostrar detalle; su suma siempre
  // debe coincidir con el monto de esta línea, nunca se vuelve a sumar aparte.
  sublineas?: WaterEgresoLineaModel[];
}

export interface WaterEgresoModel {
  aguaEgresosId?: number;
  fechaPago: string;
  fechaPagoStr?: string;
  fechaCompra?: string;
  fechaCompraStr?: string;
  monto: number;
  descripcion?: string;
  proveedor?: string;
  noFolio?: string;
  justificacion?: string;
  tipoComprobanteId?: number;
  tipoComprobanteNombre?: string;
  personaId?: number;
  personaNombre?: string;

  // Solo en registros históricos (previos al diseño de líneas): concepto
  // directo de la cabecera, cuando todo el egreso era de una sola categoría.
  conceptoId?: number;
  conceptoNombre?: string;

  // Control de limpieza histórica (solo aplica al vale, no a sus líneas).
  revisado?: boolean;
  validadoFisico?: boolean;

  lineas: WaterEgresoLineaModel[];
}
