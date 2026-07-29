// Persona que vive en una casa pero aún no es un usuario de agua formal
// (tabla agua_preregistro).
export const PREREGISTRO_ESTATUS_PENDIENTE = 1;
export const PREREGISTRO_ESTATUS_CONVERTIDO = 2;
export const PREREGISTRO_ESTATUS_DESCARTADO = 3;

export interface PreregistroUsuarioModel {
  preregistroId: number;
  casaId: number;
  nombre: string;
  telefono: string;
  observaciones: string;
  fechaRegistro: string;
  estatus: number;
  motivoPendiente: string;
  aguaUsuarioIdConvertido: number | null;
}

export interface PreregistroUsuarioCreateModel {
  nombre: string;
  telefono?: string;
  observaciones?: string;
  motivoPendiente?: string;
}
