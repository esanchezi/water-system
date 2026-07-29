export interface WaterEgresoGastoModel {
  aguaEgresosId?: number;
  fechaPago: string;
  fechaPagoStr?: string;
  conceptoId: number;
  conceptoNombre?: string;
  monto: number;
  descripcion?: string;
  proveedor?: string;
  personaId?: number;
  personaNombre?: string;
  // Opcional: si el gasto ya tiene su propia nota/factura/remisión. Si se
  // deja vacío, es justo el tipo de gasto que se agrupa en un "Vale caja".
  tipoComprobanteId?: number;
  tipoComprobanteNombre?: string;
}

export interface WaterEgresoEmitirModel {
  fechaPago: string;
  tipoComprobanteId?: number;
  noFolio?: string;
  descripcion?: string;
  justificacion?: string;
  conceptoId?: number;
  gastoIds: number[];
  valeIds?: number[];
}
