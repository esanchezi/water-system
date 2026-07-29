// Representa un integrante del censo de un usuario (tabla agua_usuario_censo)
export interface WaterUserCensusModel {
  censoId: number;
  edad: number | null;
  anioRegistro: number | null;
  observaciones: string;
  estatus: number;
  // Calculada por el backend: edad + (añoActual - anioRegistro). Null si no
  // se capturó edad ("sin clasificar").
  edadActual: number | null;
}

// Payload para agregar/editar un integrante
export interface WaterUserCensusCreateModel {
  edad?: number | null;
  observaciones?: string;
}

export interface RangoEdadModel {
  label: string;
  cantidad: number;
}

export interface WaterUserCensusResumenModel {
  rangos: RangoEdadModel[];
  sinClasificar: number;
  totalPersonas: number;
}
