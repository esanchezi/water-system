import { CatalogOptionModel } from "./Catalog.model";
import { PersonModel } from "./Person.model";

export interface ProcessModel {
    id: number;
    fechaEntrega : string;
    person : PersonModel;
    tipoTramite:CatalogOptionModel;
    estatusTramite:CatalogOptionModel;
  }