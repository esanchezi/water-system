// Foto de respaldo de la entrega de una carta de adeudo -- típicamente
// cuando no se encontró al usuario (tipoEntrega = NO_ENCONTRADO). Ver
// AvisoAdeudoFotoEntity en el backend.
export interface AvisoAdeudoFotoModel {
  fotoId: number;
  nombreArchivo?: string;
  nombreOriginal?: string;
  contentType?: string;
  avisoAdeudoId?: number;
  dateAdd?: string;
}
