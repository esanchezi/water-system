// Un usuario de una calle, candidato a recibir un Aviso por uso indebido de
// bomba (Art. 23) -- a diferencia de Cartas de adeudo, NO hay cálculo de
// deuda: cualquier usuario activo de la calle aplica por igual.
export interface AvisoBombaCandidatoModel {
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

// Un aviso de bomba ya generado (histórico) -- snapshot de lo que se
// imprimió, mismo patrón que AvisoAdeudoModel pero sin nada de adeudo.
export interface AvisoBombaModel {
  avisoBombaId: number;
  folioNotificacion: number;
  aguaUsuarioId?: number;
  noUsuario: number;
  nombreUsuarioTitular: string;
  noCasa?: number;
  noCasaTexto?: string;
  domicilioToma?: string;
  fechaReporte?: string;
  dateAdd: string;

  // Se oculta del historial por default (mismo patrón que avisos de
  // adeudo) pero se puede consultar explícitamente.
  cancelado?: boolean;

  // Registro de entrega -- ver AvisoBombaEntregaModel/TIPOS_ENTREGA (mismas
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

// Datos que se capturan al marcar un aviso de bomba como entregado -- mismo
// shape que AvisoAdeudoEntregaModel.
export interface AvisoBombaEntregaModel {
  tipoEntrega: string;
  nombreReceptor?: string;
  parentescoReceptor?: string;
  nombreNotificador?: string;
  nombreTestigo1?: string;
  nombreTestigo2?: string;
  comentarioEntrega?: string;
  fechaEntrega?: string;
}
