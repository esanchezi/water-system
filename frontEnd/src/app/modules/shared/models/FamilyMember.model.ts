import { CatalogOptionModel } from "./Catalog.model";
import { PersonModel } from "./Person.model";

export interface FamilyMemberModel {
    id: number;
    observaciones : string;
    person : PersonModel;
    tipoFamilia:CatalogOptionModel;
  }