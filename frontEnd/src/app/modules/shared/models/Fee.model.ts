import { CatalogOptionModel } from "./Catalog.model";

export interface FeeModel {
  cuotaId: number;
  uso: CatalogOptionModel;
  userType: CatalogOptionModel;
  observaciones: string;
  amount:FeeAmountModel[]
}

export interface FeeAmountModel {
  catalogoOpcionesId: number;
  cuotaMontoId: number;
  cuotaId: number;
  cuota: number;
  vigencia: number;
  observaciones: string;
  uso: string;
  type:string;
}

// Payload para crear/editar una categoría de cuota (uso + tipo de usuario)
export interface FeeCreateModel {
  usoId: number;
  userTypeId: number;
  observaciones: string;
}

// Payload para agregar/editar el monto de un año (cuota_monto)
export interface FeeAmountCreateModel {
  cuota: number;
  vigencia: number;
  observaciones: string;
}

