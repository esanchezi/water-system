// Un aviso de instalación de medidor ya generado (histórico) -- snapshot de
// lo que se imprimió, mismo patrón que AvisoBombaModel pero con los campos
// propios de esta carta (fecha de instalación, número de serie, lectura
// inicial) en vez de adeudo/reporte.
export interface AvisoMedidorModel {
  avisoMedidorId: number;
  folioNotificacion: number;
  aguaUsuarioId?: number;
  noUsuario: number;
  nombreUsuarioTitular: string;
  noCasa?: number;
  noCasaTexto?: string;
  domicilioToma?: string;
  fechaInstalacion?: string;
  noSerieMedidor?: string;
  lecturaInicialMedidor?: string;
  dateAdd: string;

  // Se oculta del historial por default (mismo patrón que el resto de
  // avisos) pero se puede consultar explícitamente.
  cancelado?: boolean;

  // Registro de entrega -- ver AvisoMedidorEntregaModel/TIPOS_ENTREGA
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

// Datos que se capturan al marcar un aviso de medidor como entregado --
// mismo shape que AvisoBombaEntregaModel.
export interface AvisoMedidorEntregaModel {
  tipoEntrega: string;
  nombreReceptor?: string;
  parentescoReceptor?: string;
  nombreNotificador?: string;
  nombreTestigo1?: string;
  nombreTestigo2?: string;
  comentarioEntrega?: string;
  fechaEntrega?: string;
}
