import { CatalogOptionModel } from "./Catalog.model";

export interface WaterUserNotifyModel{
    id: number;
    // Id real que espera el backend (WaterUserNoticeDto.aguaUsuarioAvisoId)
    // para actualizar el estatus -- 'id' se conserva por compatibilidad,
    // pero no coincide con el nombre del campo del backend.
    aguaUsuarioAvisoId?: number;
    estatusAviso:CatalogOptionModel;
    aviso : string;
    comentario: string;
    tipo: CatalogOptionModel;
    responsable: CatalogOptionModel;
  }
