// Aviso Informativo de Adeudo: notificación PREVIA y más suave que las
// Cartas de adeudo (ver AvisoAdeudo.model.ts) -- solo para ciertos usuarios
// elegidos A MANO por el Comité, sin cálculo automático de deuda. Folio
// propio, totalmente independiente del de Cartas de adeudo.

// Un usuario elegido a mano para agregar a la lista que se va a generar --
// la selección es manual, pero el adeudo/multa/último pago se calculan en
// el backend igual que en Cartas de adeudo (AdeudoLuzService), no se
// capturan aquí.
export interface UsuarioInformativoAdeudoModel {
  aguaUsuarioId: number;
  // Solo para mostrarlo en la tabla mientras se arma la lista -- no se
  // manda al backend (usa aguaUsuarioId).
  noUsuario?: number;
  nombreCompleto?: string;
  // Nota libre del Comité (opcional) -- lo único que sigue siendo manual.
  observacion?: string;
}

// Un aviso informativo ya generado (histórico) -- snapshot de lo impreso.
export interface AvisoInformativoAdeudoModel {
  avisoInformativoAdeudoId: number;
  folioNotificacion: number;
  aguaUsuarioId?: number;
  noUsuario: number;
  nombreUsuarioTitular: string;
  noCasa?: number;
  noCasaTexto?: string;
  domicilioToma?: string;
  periodosAdeudados?: string;
  adeudoTotal?: number;
  multaAcumulada?: number;
  noFolioUltimoPago?: number;
  fechaUltimoPago?: string;
  // Fecha completa en la que debía presentarse en el Comité, elegida al
  // generar -- para poder darle seguimiento.
  fechaPresentacion?: string;
  observacion?: string;
  dateAdd: string;

  cancelado?: boolean;

  // Registro de entrega -- mismas opciones que TIPOS_ENTREGA (sin ABONO,
  // este módulo no maneja cobro/abono).
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

// Datos que se capturan al marcar un aviso informativo como entregado --
// mismo shape que AvisoBombaEntregaModel.
export interface AvisoInformativoAdeudoEntregaModel {
  tipoEntrega: string;
  nombreReceptor?: string;
  parentescoReceptor?: string;
  nombreNotificador?: string;
  nombreTestigo1?: string;
  nombreTestigo2?: string;
  comentarioEntrega?: string;
  fechaEntrega?: string;
}
