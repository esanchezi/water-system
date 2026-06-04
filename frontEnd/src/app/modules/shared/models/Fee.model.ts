import { CatalogOptionModel } from "./Catalog.model";

export interface FeeModel {
  cuotaId: number;
  uso: CatalogOptionModel;
  userType: CatalogOptionModel;
  amount:FeeAmountModel[]
}

export interface FeeAmountModel {
  catalogoOpcionesId: number;
  cuotaMontoId: number;
  cuotaId: number;
  cuota: number;
  vigencia: number;
  uso: string;
  type:string;
}

