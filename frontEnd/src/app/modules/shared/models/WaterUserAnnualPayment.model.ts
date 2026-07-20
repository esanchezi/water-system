// Representa un registro de "año pagado" (tabla agua_usuario_pago_anual)
export interface WaterUserAnnualPaymentModel {
  pagoAnualId: number;
  anio: number;
  fechaValidacion: string;
  observaciones: string;
  estatus: number;
}

// Payload para marcar un año como pagado
export interface WaterUserAnnualPaymentCreateModel {
  anio: number;
  fechaValidacion?: string;
  observaciones?: string;
}
