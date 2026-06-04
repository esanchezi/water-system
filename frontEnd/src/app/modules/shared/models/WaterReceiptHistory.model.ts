
import { WaterUserModel } from './WaterUser.model';

export interface WaterReceiptHistoryModel {
  aguaReciboHistorialId: number;
  noFolio: string;
  esProcesado:boolean;
  fecha: string;
  fechaStr: string;
  nombre: string;
  calle: string;
  numero: string;
  colonia: string;
  concepto: string;
  observaciones: string;
  monto: number;
  habitaDomicilio: boolean;
  tieneToma: boolean;
  email: string;
  waterUser: WaterUserModel;
}
