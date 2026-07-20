import { CatalogOptionModel } from "./Catalog.model";

export interface WaterAgreementChargeModel {
  aguaConvenioCargoId: number;
  aguaConvenioId: number;
  aguaUsuarioCargoId: number;
  conceptoCargo: string;
  montoCargo: number;
  montoCondonado: number;
}

export interface WaterAgreementModel {
  aguaConvenioId: number;
  noFolio: number;
  noUsuario: number;
  nombreUsuario: string;
  fecha: string;
  fechaStr: string;
  motivo: string;
  comentario: string;
  adeudo: number;
  fechaCompromisoPago: string;
  fechaCompromisoPagoStr: string;
  estatusConvenioId: number;
  estatusConvenio: CatalogOptionModel;
  cargos: WaterAgreementChargeModel[];
  montoCondonadoTotal: number;
}
