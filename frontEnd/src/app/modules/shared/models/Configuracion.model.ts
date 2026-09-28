// Ajustes generales de la app, guardados como llave/valor -- así se pueden
// agregar más ajustes después (logo, moneda...) sin cambiar el esquema.
export interface ConfiguracionModel {
  configuracionId?: number;
  clave: string;
  valor: string;
  descripcion?: string;
  estatus?: number;
}

// Llaves conocidas. La tabla admite más sin necesitar cambios de esquema.
export const CLAVE_NOMBRE_COMITE = 'NOMBRE_COMITE';
export const CLAVE_BLOQUEADO = 'BLOQUEADO';
