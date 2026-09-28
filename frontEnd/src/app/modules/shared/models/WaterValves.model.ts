// Modelo del nuevo módulo de pozos / tramos / cajas de válvulas.
// Ver agua-pozo, agua-tramo, agua-caja-valvula, agua-valvula,
// agua-registro_suministro en el backend.

export interface PozoModel {
  pozoId: number;
  nombre: string;
  lat?: number | null;
  lng?: number | null;
  observaciones?: string;
  estatus?: number;
}

export interface PuntoTrazo {
  lat: number;
  lng: number;
}

export type DiaSemana = 'LUNES' | 'MARTES' | 'MIERCOLES' | 'JUEVES' | 'VIERNES' | 'SABADO' | 'DOMINGO';

// Un mismo tramo puede tener más de un horario (ej. "Jazmín" -> jueves de
// 6 a 10am del pozo Buenavista, Y domingo de 16 a 18 del mismo pozo).
export interface TramoHorarioModel {
  horarioId?: number;
  pozoId: number | null;
  pozoNombre?: string;
  diaSemana: DiaSemana | string | null;
  // Formato "HH:mm", tal cual lo da un <input type="time">.
  horaInicio: string | null;
  horaFin: string | null;
}

// Qué válvulas hay que abrir/cerrar para que este tramo reciba agua. Una
// misma válvula puede aparecer en varios tramos con distinto estado
// requerido (ej. abierta para el tramo de la mañana, cerrada para el de
// la tarde).
export interface TramoValvulaModel {
  tramoValvulaId?: number;
  valvulaId: number | null;
  valvulaIdentificador?: string;
  cajaId?: number;
  cajaNombre?: string;
  cajaLado?: string | null;
  // ABIERTA / CERRADA
  estadoRequerido: 'ABIERTA' | 'CERRADA' | string | null;
}

// Segmento de tubería/manguera dentro de un tramo -- un mismo tramo puede
// combinar más de un material/diámetro a lo largo de su trazo (ej. PVC de
// 2" al inicio y manguera de 1" después), por eso es una lista, en el
// orden en que van desde el origen (pozo/caja) hacia el final del tramo.
export interface SegmentoTuberiaModel {
  segmentoId?: number;
  orden?: number | null;
  // PVC / POLIDUCTO / MANGUERA / GALVANIZADO / OTRO -- texto libre, ver
  // MATERIALES_SEGMENTO para las opciones que se sugieren en el formulario.
  material?: string | null;
  // Texto libre (ej. "1\"", "3/4", "2 pulg").
  diametro?: string | null;
  longitudMetros?: number | null;
  observaciones?: string | null;
}

export const MATERIALES_SEGMENTO = ['PVC', 'POLIDUCTO', 'MANGUERA', 'GALVANIZADO', 'OTRO'];

export interface TramoModel {
  tramoId: number;
  nombre: string;
  descripcion?: string;
  // IZQUIERDO / DERECHO, opcional -- para calles con 2 redes, donde cada
  // lado es su propio tramo (su propio horario) pero conviene marcarlos
  // así para no confundirlos ni depender solo del nombre.
  lado?: string | null;
  // Arreglo de puntos ya parseado (viene como JSON string desde el back).
  trazoJson?: string;
  metodoTrazo?: 'DIRECTIONS' | 'MANUAL' | string;
  estatus?: number;

  // Lista de horarios de esta ruta de suministro (pozo + día + turno).
  horarios?: TramoHorarioModel[];

  // Lista de válvulas que hay que abrir/cerrar para este tramo.
  valvulas?: TramoValvulaModel[];

  // Segmentos de tubería/manguera que forman este tramo -- opcional, no
  // todos los tramos lo necesitan capturado a este nivel de detalle.
  segmentos?: SegmentoTuberiaModel[];
}

// Referencia a una foto ya subida -- el archivo real se pide aparte (ver
// ValvulaFotoService.getArchivoBlob), esto solo trae el id/nombre.
export interface ValvulaFotoModel {
  fotoId: number;
  nombreArchivo?: string;
  nombreOriginal?: string;
  contentType?: string;
  valvulaId?: number;
}

export interface ValvulaModel {
  valvulaId?: number;
  identificador: string;
  tipo?: string;
  // ABIERTA / CERRADA
  estado?: string;
  observaciones?: string;
  estatus?: number;
  cajaId?: number;
  fotos?: ValvulaFotoModel[];
}

export interface CajaValvulaModel {
  cajaId?: number;
  codigo?: string;
  nombre: string;
  lat?: number | null;
  lng?: number | null;
  observaciones?: string;
  // Lado de la calle (IZQUIERDO / DERECHO), solo para calles con 2 redes
  // independientes -- opcional, sirve para no ver todas las cajas de un
  // tramo mezcladas.
  lado?: string | null;
  estatus?: number;
  tramoId?: number | null;
  tramoNombre?: string;
  listValvula?: ValvulaModel[];
}

export interface RegistroSuministroTramoModel {
  registroTramoId?: number;
  orden: number;
  tramoId: number;
  tramoNombre?: string;
}

export interface RegistroSuministroModel {
  registroId?: number;
  fecha: string; // yyyy-MM-dd
  turno: 'MATUTINO' | 'VESPERTINO' | string;
  observaciones?: string;
  estatus?: number;
  pozoId: number;
  pozoNombre?: string;
  listTramos?: RegistroSuministroTramoModel[];
}

export interface DiasPorPozoModel {
  pozoId: number;
  pozoNombre: string;
  totalDias: number;
}

export interface DiasPorTramoModel {
  tramoId: number;
  tramoNombre: string;
  totalDias: number;
}

export interface ResumenValvulasModel {
  totalCajas: number;
  totalValvulas: number;
}
