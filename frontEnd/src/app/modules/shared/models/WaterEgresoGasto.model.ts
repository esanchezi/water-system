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
}

export interface WaterEgresoEmitirModel {
  fechaPago: string;
  tipoComprobanteId?: number;
  noFolio?: string;
  descripcion?: string;
  justificacion?: string;
  gastoIds: number[];
}
