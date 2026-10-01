// Foto de respaldo de la entrega de un aviso de actualización de padrón --
// ej. cuando no se encontró al usuario o se negó a firmar. Ver
// AvisoPadronFotoEntity en el backend.
export interface AvisoPadronFotoModel {
  fotoId: number;
  nombreArchivo?: string;
  nombreOriginal?: string;
  contentType?: string;
  avisoPadronId?: number;
  dateAdd?: string;
}
