import { CatalogOptionModel } from "./Catalog.model";

export interface WaterUserNotifyModel{
    id: number;
    estatusAviso:CatalogOptionModel;
    aviso : string;
    comentario: string;
    tipo: CatalogOptionModel;
    responsable: CatalogOptionModel;
  }
