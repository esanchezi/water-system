// Foto de respaldo de la entrega de un Aviso Informativo de Adeudo -- ej.
// cuando no se encontró al usuario o se negó a firmar. Ver
// AvisoInformativoAdeudoFotoEntity en el backend.
export interface AvisoInformativoAdeudoFotoModel {
  fotoId: number;
  nombreArchivo?: string;
  nombreOriginal?: string;
  contentType?: string;
  avisoInformativoAdeudoId?: number;
  dateAdd?: string;
}
