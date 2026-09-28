// Candidato a carta de adeudo -- usuario con saldo pendiente de la
// aportación de Luz/CFE (concepto_id = 6) en uno o más años. Ver
// AdeudoLuzService en el backend.
export interface AdeudoLuzUsuarioModel {
  aguaUsuarioId: number;
  noUsuario: number;
  nombreCompleto: string;
  casaNo?: number;
  // "2-D" / "2-I" -- casaNo + lado de la casa, cuando se conoce.
  casaNoTexto?: string;
  calleNombre?: string;
  domicilioToma?: string;
  // Estatus del Comité (catálogo) -- para identificar de un vistazo a
  // quiénes ya tienen corte del servicio.
  estatusComiteId?: number;
  estatusComiteNombre?: string;
  periodosAdeudados: number[];
  periodosAdeudadosTexto: string;
  adeudoTotal: number;
  noFolioUltimoPago?: number;
  fechaUltimoPago?: string;

  // Control de Primer/Segundo aviso -- último aviso ACTIVO que se le
  // generó a este usuario (null si nunca se le ha generado ninguno).
  ultimoTipoAviso?: string;
  ultimoAvisoEntregado?: boolean;
  ultimoAvisoFechaEntrega?: string;
  // true si el último aviso fue Primero y ya se entregó -- lo que sigue es
  // generarle el Segundo, no repetir el Primero. En false mientras
  // enConvenioVigente sea true, aunque por lo demás ya le tocaría.
  requiereSegundoAviso?: boolean;

  // true si el usuario tiene un convenio de pago activo cuya fecha
  // comprometida todavía no vence -- mientras esté vigente, se pausa el
  // Segundo aviso (ya vino a hacer un arreglo con el comité).
  enConvenioVigente?: boolean;
  fechaCompromisoConvenio?: string;
}

// Un aviso ya generado (histórico) -- snapshot de lo que se imprimió,
// no necesariamente igual a los datos actuales del usuario.
export interface AvisoAdeudoModel {
  avisoAdeudoId: number;
  folioNotificacion: number;
  tipoAviso: string;
  // PK real del usuario -- distinta de noUsuario (el número de cuenta
  // visible). Se usa para el botón "Generar Segundo aviso" directo desde
  // el historial, que necesita este id para volver a calcular su estado
  // actual (ver AvisoAdeudoService.candidatoUnico() en el backend).
  aguaUsuarioId?: number;
  noUsuario: number;
  nombreUsuarioTitular: string;
  noCasa?: number;
  noCasaTexto?: string;
  domicilioToma?: string;
  periodosAdeudados: string;
  adeudoTotal: number;
  noFolioUltimoPago?: number;
  fechaUltimoPago?: string;
  dateAdd: string;

  // Se oculta del historial por default (mismo patrón que los usuarios
  // dados de baja) pero se puede consultar explícitamente.
  cancelada?: boolean;

  // Registro de entrega -- ver AvisoAdeudoEntregaModel/TIPOS_ENTREGA.
  entregado?: boolean;
  fechaEntrega?: string;
  tipoEntrega?: string;
  nombreReceptor?: string;
  parentescoReceptor?: string;
  nombreNotificador?: string;
  nombreTestigo1?: string;
  nombreTestigo2?: string;

  // true en cuanto se hace el cobro/trámite correspondiente tras la
  // entrega -- mientras sea false, la ficha del usuario sigue alertando.
  atendido?: boolean;
  fechaAtencion?: string;
  // Cómo se resolvió -- ver RESULTADOS_ATENCION. Se captura junto con
  // fechaAtencion, ver AvisoAdeudoAtencionModel.
  resultadoAtencion?: string;
  comentarioAtencion?: string;
  // Folio del recibo (agua_recibo) vinculado como comprobante, si se
  // capturó -- solo referencia, el monto real vive en el recibo.
  folioReciboVinculado?: number;
  // Folio del convenio (módulo Convenios) vinculado como referencia, si se
  // capturó -- independiente de folioReciboVinculado, pueden venir ambos
  // (ej. el convenio cubre parte y hay un recibo por el resto).
  folioConvenioVinculado?: number;
  // Solo cuando resultadoAtencion = CONVENIO: fecha comprometida de pago.
  // Mientras no venza, se pausa el Segundo aviso de este usuario.
  fechaCompromiso?: string;
}

export const TIPOS_AVISO: { valor: string; etiqueta: string }[] = [
  { valor: 'PRIMERO', etiqueta: 'Primer aviso' },
  { valor: 'SEGUNDO', etiqueta: 'Segundo aviso' },
];

// Datos que se capturan al marcar una carta como entregada -- misma
// sección "RAZÓN DE NOTIFICACIÓN" que trae la carta física.
export interface AvisoAdeudoEntregaModel {
  tipoEntrega: string;
  nombreReceptor?: string;
  parentescoReceptor?: string;
  nombreNotificador?: string;
  nombreTestigo1?: string;
  nombreTestigo2?: string;
  fechaEntrega?: string;
}

// Las 4 opciones "marcar lo que corresponda" de la carta física, en el
// mismo orden en que aparecen ahí.
export const TIPOS_ENTREGA: { valor: string; etiqueta: string }[] = [
  { valor: 'TITULAR', etiqueta: 'Entregué el documento a la persona titular de la toma, quien firmó de recibido' },
  { valor: 'OTRA_PERSONA', etiqueta: 'Entregué el documento a otra persona, quien dijo ser del titular y firmó de recibido' },
  { valor: 'SE_NEGO', etiqueta: 'La persona que atendió se negó a recibir o a firmar' },
  { valor: 'NO_ENCONTRADO', etiqueta: 'No se encontró a persona alguna' },
];

// Datos que se capturan al marcar un aviso como atendido -- cómo se
// resolvió (categoría fija), un comentario opcional para el detalle, y el
// folio del recibo real vinculado como comprobante (opcional, el monto en
// sí vive en el recibo, aquí solo se guarda la referencia).
export interface AvisoAdeudoAtencionModel {
  resultadoAtencion: string;
  comentarioAtencion?: string;
  folioReciboVinculado?: number;
  // Folio del convenio (módulo Convenios) vinculado como referencia,
  // opcional e independiente del recibo -- pueden ir ambos a la vez.
  folioConvenioVinculado?: number;
  // Obligatoria cuando resultadoAtencion = CONVENIO -- ver
  // AvisoAdeudoAtencionDialogComponent.
  fechaCompromiso?: string;
}

export const RESULTADOS_ATENCION: { valor: string; etiqueta: string }[] = [
  { valor: 'PAGADO', etiqueta: 'Pagado' },
  { valor: 'CONDONADO', etiqueta: 'Condonado (falta de información en el aviso)' },
  { valor: 'CONVENIO', etiqueta: 'Convenio de pago' },
  { valor: 'OTRO', etiqueta: 'Otro' },
];

// Persona SIN usuario en el sistema (censo en proceso) a la que también se
// le quiere generar carta -- solo nombre (obligatorio) y dirección
// (opcional). El adeudo se calcula con la cuota fija anual del backend
// (ver AvisoAdeudoService.CUOTAS_FIJAS_NO_REGISTRADO), no se guarda en el
// historial.
export interface UsuarioNoRegistradoModel {
  nombre: string;
  direccion?: string;
}
