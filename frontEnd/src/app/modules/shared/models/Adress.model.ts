import { CatalogOptionModel } from "./Catalog.model";

export interface AdressModel {
    id: number;
    direccionId: number;
    calle : string;
    numero: string;
    referencia: string;
    entrecalle1: string;
    entrecalle2: string;
    fkIdSeccion: string;
    seccion:CatalogOptionModel;
  }