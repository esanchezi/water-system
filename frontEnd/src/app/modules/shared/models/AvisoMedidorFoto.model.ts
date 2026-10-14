// Foto de respaldo de la entrega de un aviso de instalación de medidor --
// ej. cuando no se encontró al usuario o se negó a firmar. Ver
// AvisoMedidorFotoEntity en el backend.
export interface AvisoMedidorFotoModel {
  fotoId: number;
  nombreArchivo?: string;
  nombreOriginal?: string;
  contentType?: string;
  avisoMedidorId?: number;
  dateAdd?: string;
}
