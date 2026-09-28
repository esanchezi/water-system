// Historial de revisiones de un usuario -- física (toma/medidor), de datos
// de padrón, o general. Ver WaterUserRevisionEntity en el backend.
export interface WaterUserRevisionModel {
  revisionId: number;
  fecha: string;
  tipo: string;
  resultado: string;
  comentario?: string;
  userIdAdd?: number;
  dateAdd?: string;
  aguaUsuarioId?: number;
}

// Lo que se manda al registrar una revisión nueva.
export interface WaterUserRevisionRequestModel {
  fechaStr: string;
  tipo: string;
  resultado: string;
  comentario?: string;
}

export interface WaterUserRevisionFotoModel {
  fotoId: number;
  nombreArchivo: string;
  nombreOriginal: string;
  contentType: string;
  revisionId: number;
  dateAdd?: string;
}

// Catálogo fijo de tipos y resultados -- no amerita tabla de catálogo
// aparte (mismo criterio que TIPOS_AVISO/TIPOS_ENTREGA en AvisoAdeudo.model.ts).
export const TIPOS_REVISION: { value: string; label: string }[] = [
  { value: 'TOMA_MEDIDOR', label: 'Toma / medidor' },
  { value: 'DATOS_PADRON', label: 'Datos de padrón' },
  { value: 'GENERAL', label: 'General' },
];

export const RESULTADOS_REVISION: { value: string; label: string }[] = [
  { value: 'BIEN', label: 'Bien' },
  { value: 'CON_PROBLEMA', label: 'Con problema' },
];
