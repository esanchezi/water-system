import { CatalogOptionModel } from "./Catalog.model";

export interface WaterUserChargePaymentModel {
  aguaUsuarioCargoPagoId: number;
  aguaUsuarioCargoId: number;
  reciboId: number;
  noFolio: number;
  montoAplicado: number;
  fechaPagoStr: string;
}

export interface WaterUserChargeModel {
  aguaUsuarioCargoId: number;
  noUsuario: number;
  // Solo viene lleno en listados que juntan cargos de varios usuarios a la
  // vez (ej. lista de multas de un grupo completo).
  nombreUsuario?: string;
  conceptoId: number;
  concepto: CatalogOptionModel;
  descripcion: string;
  monto: number;
  fecha: string;
  fechaStr: string;
  comentario: string;
  // Solo se marca cuando aplica (ej. multas por manipular válvulas o
  // reconexión no autorizada) -- cuando es true, la carta de adeudo agrega
  // "(aprobado por asamblea el dd/mm/aaaa)" en el desglose de "Multa
  // acumulada" junto a este cargo. fechaAsamblea puede quedar sin capturar
  // aunque aprobadoAsamblea sea true.
  aprobadoAsamblea?: boolean;
  fechaAsamblea?: string;
  fechaAsambleaStr?: string;
  pagos: WaterUserChargePaymentModel[];
  montoPagado: number;
  montoCondonado: number;
  saldo: number;
  pagado: boolean;
}
