export interface CatalogModel {
  catalogoId: number;
  clave: string;
  nombre: string;
  descripcion: string;
  estatus: number;
  options: CatalogOptionModel[];
}

export interface CatalogOptionModel {
  catalogoOpcionesId: number;
  catalogoId: number;
  nombre: string;
  descripcion: string;
  estatus: number;
}

export interface CatalogRestResponse {
  data: CatalogModel[];
  metadata: {
    code: string;
    message: string;
    detail: string;
  };
}

// Mantenido para compatibilidad con módulos existentes
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
