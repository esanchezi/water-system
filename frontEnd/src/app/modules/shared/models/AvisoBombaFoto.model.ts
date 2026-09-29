// Foto de respaldo de la entrega de un aviso de uso de bomba -- ej. cuando
// no se encontró al usuario o se negó a firmar. Ver AvisoBombaFotoEntity en
// el backend.
export interface AvisoBombaFotoModel {
  fotoId: number;
  nombreArchivo?: string;
  nombreOriginal?: string;
  contentType?: string;
  avisoBombaId?: number;
  dateAdd?: string;
}
