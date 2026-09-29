// Usuario ya dado de alta y ligado a una casa, con su cuota vigente -- para
// el selector "agregar usuario existente" del formulario (Ely pidió poder
// ver la cuota que ya están pagando antes de agregarlos). cuotaVigente puede
// venir null si el usuario no tiene cuota asignada o no hay monto capturado
// para el año actual.
export interface CasaUsuarioCuotaModel {
  aguaUsuarioId: number;
  noUsuario: number;
  nombreCompleto: string;
  cuotaVigente?: number;
}

// Un renglón de la tabla "RESOLUCIÓN DEL COMITÉ" -- puede ser una persona
// SIN usuario dado de alta todavía (solo nombreCompleto libre) o, si se
// eligió del selector de usuarios de la casa, queda ligada a su
// aguaUsuarioId (ver CasaUsuarioCuotaModel) y trae el snapshot de la cuota
// que tenía en ese momento.
export interface AvisoResponsablePagoPersonaModel {
  responsablePagoPersonaId?: number;
  orden?: number;
  nombreCompleto: string;
  parentesco?: string;
  familiaCuota?: number;
  aguaUsuarioId?: number;
  noUsuario?: number;
  cuotaVigenteSnapshot?: number;
}

// Un aviso de responsables de pago ya generado (histórico) -- snapshot de
// lo que se imprimió. A diferencia de las demás cartas, se liga a una Casa,
// no a un usuario (ver AvisoResponsablePagoEntity en el backend).
export interface AvisoResponsablePagoModel {
  responsablePagoId: number;
  folioNotificacion: number;
  casaId?: number;
  noCasa?: number;
  noCasaTexto?: string;
  domicilioToma?: string;
  motivoSolicitud?: string;
  fechaSolicitud?: string;
  observacionesComite?: string;
  personas: AvisoResponsablePagoPersonaModel[];
  dateAdd: string;

  // Se oculta del historial por default (mismo patrón que las demás
  // cartas) pero se puede consultar explícitamente.
  cancelado?: boolean;

  // Registro de entrega -- ver AvisoResponsablePagoEntregaModel/TIPOS_ENTREGA
  // (mismas opciones que en Cartas de adeudo, ver AvisoAdeudo.model.ts).
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

// Datos que se capturan al marcar un aviso como entregado -- mismo shape
// que AvisoAdeudoEntregaModel/AvisoPadronEntregaModel.
export interface AvisoResponsablePagoEntregaModel {
  tipoEntrega: string;
  nombreReceptor?: string;
  parentescoReceptor?: string;
  nombreNotificador?: string;
  nombreTestigo1?: string;
  nombreTestigo2?: string;
  comentarioEntrega?: string;
  fechaEntrega?: string;
}
