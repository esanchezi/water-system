// Un usuario de una calle, candidato a recibir un Aviso para actualización
// del padrón de habitantes (Art. 5, 15, 15 Bis y 15 Ter) -- igual que Aviso
// de bomba, NO hay cálculo de deuda: cualquier usuario activo de la calle
// aplica por igual.
export interface AvisoPadronCandidatoModel {
  aguaUsuarioId: number;
  noUsuario: number;
  nombreCompleto: string;
  casaNo?: number;
  casaNoTexto?: string;
  calleNombre?: string;
  domicilioToma?: string;
  estatusComiteId?: number;
  estatusComiteNombre?: string;
}

// Un aviso de padrón ya generado (histórico) -- snapshot de lo que se
// imprimió, mismo patrón que AvisoBombaModel/AvisoAdeudoModel pero sin nada
// de adeudo.
export interface AvisoPadronModel {
  avisoPadronId: number;
  folioNotificacion: number;
  aguaUsuarioId?: number;
  noUsuario: number;
  nombreUsuarioTitular: string;
  noCasa?: number;
  noCasaTexto?: string;
  domicilioToma?: string;
  // "Debe presentarse el día ___ de ___ de 20__" -- fecha completa elegida
  // al generar (mismo criterio que fechaPresentacion en Cartas de adeudo).
  fechaPresentacion?: string;
  // Motivo por el que se solicitó la actualización -- opcional.
  motivoSolicitud?: string;
  dateAdd: string;

  // Se oculta del historial por default (mismo patrón que avisos de
  // adeudo/bomba) pero se puede consultar explícitamente.
  cancelado?: boolean;

  // Registro de entrega -- ver AvisoPadronEntregaModel/TIPOS_ENTREGA (mismas
  // opciones que en Cartas de adeudo, ver AvisoAdeudo.model.ts).
  entregado?: boolean;
  fechaEntrega?: string;
  tipoEntrega?: string;
  nombreReceptor?: string;
  parentescoReceptor?: string;
  nombreNotificador?: string;
  nombreTestigo1?: string;
  nombreTestigo2?: string;
  comentarioEntrega?: string;
}

// Datos que se capturan al marcar un aviso de padrón como entregado --
// mismo shape que AvisoAdeudoEntregaModel/AvisoBombaEntregaModel.
export interface AvisoPadronEntregaModel {
  tipoEntrega: string;
  nombreReceptor?: string;
  parentescoReceptor?: string;
  nombreNotificador?: string;
  nombreTestigo1?: string;
  nombreTestigo2?: string;
  comentarioEntrega?: string;
  fechaEntrega?: string;
}
