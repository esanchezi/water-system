
import { WaterUserModel } from './WaterUser.model';

export interface WaterReceiptModel {
  aguaReciboId: number;
  noFolio: number;
  fecha: string;
  observaciones: string;
  concepto: string;
  total: number;
  habitaDomicilio: boolean;
  tieneToma: boolean;
  email: string;
  waterUser: WaterUserModel;
  waterReceiptPayment:WaterReceiptPaymentModel[]
}

export interface WaterReceiptPaymentModel {
  aguaReciboPagoId: number;
  montoRecibido: number;
  montoAplicado: number;
  concepto: string;
  anio: number;
  fechaPago: string;
}
