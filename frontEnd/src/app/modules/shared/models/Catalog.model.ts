export interface CatalogModel {
  catalogoId: number;
  nombre: string;
  descripcion: string;
}

export interface CatalogOptionModel {
  catalogoOpcionesId: number;
  nombre: string;
  descripcion: string;
}

export interface CatalogData {
  cat1: CatalogOptionModel[];
  cat2: CatalogOptionModel[];
  cat3: CatalogOptionModel[];
  cat4: CatalogOptionModel[];
  cat5: CatalogOptionModel[];
  cat6: CatalogOptionModel[];
}

export interface CatalogOption {
  catalogoOpcionesId: number;
  nombre: string;
}

export interface CatalogItem {
  options: CatalogOption[];
}

export interface CatalogResponse {
  metadata: any[];
  data: CatalogItem[];
}