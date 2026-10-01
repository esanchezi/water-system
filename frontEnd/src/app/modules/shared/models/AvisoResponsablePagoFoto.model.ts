// Foto de respaldo de la entrega de un aviso de responsables de pago -- ej.
// cuando no se encontró a nadie o se negó a firmar. Ver
// AvisoResponsablePagoFotoEntity en el backend.
export interface AvisoResponsablePagoFotoModel {
  fotoId: number;
  nombreArchivo?: string;
  nombreOriginal?: string;
  contentType?: string;
  responsablePagoId?: number;
  dateAdd?: string;
}
