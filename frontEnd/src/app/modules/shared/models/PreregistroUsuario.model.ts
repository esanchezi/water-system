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
  // Negocio que nunca va a tener su propio usuario de agua (ej. tiendita
  // atendida por el usuario del domicilio) pero que sí se cuenta en el
  // censo de negocios.
  esNegocio?: boolean;
  giroNegocioId?: number | null;
  giroNegocioNombre?: string;
  motivoNoUsuarioId?: number | null;
  motivoNoUsuarioNombre?: string;
  // Deuda aproximada (estimación manual, no viene de cargos reales -- esta
  // persona todavía no es usuario formal, así que no hay cargos que sumar).
  deudaAportaciones?: number | null;
  deudaMultasRecargos?: number | null;
  deudaObservaciones?: string;
  // Solo viene lleno en listados que juntan varias casas (ej. deudores).
  calleNombre?: string;
  casaNo?: number;
  // Grupo al que probablemente se va a unir esta persona cuando se
  // convierta en usuario formal (opcional).
  grupoId?: number | null;
  grupoNombre?: string;
}

export interface PreregistroUsuarioCreateModel {
  nombre: string;
  telefono?: string;
  observaciones?: string;
  motivoPendiente?: string;
  esNegocio?: boolean;
  giroNegocioId?: number | null;
  motivoNoUsuarioId?: number | null;
  deudaAportaciones?: number | null;
  deudaMultasRecargos?: number | null;
  deudaObservaciones?: string;
  grupoId?: number | null;
}
