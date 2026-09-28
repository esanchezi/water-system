// Solicitud y acta de renuncia temporal al servicio (Art. 6 Bis del
// reglamento). Ver RenunciaTemporalEntity en el backend -- mientras
// fechaReconexion sea null y no esté cancelada, el usuario se considera
// "actualmente en renuncia temporal" y se excluye de candidatos a carta
// de adeudo (ver AdeudoLuzService).
export interface RenunciaTemporalModel {
  renunciaTemporalId: number;
  folio: number;
  noUsuario?: number;
  nombreUsuario?: string;
  fechaRenuncia: string;
  motivo: string;
  // Snapshot de lo que el usuario debía (Luz + interés) al momento de
  // renunciar -- constancia por escrito, no exime el adeudo.
  adeudoALaFecha: number;
  cancelada?: boolean;

  fechaSolicitudReconexion?: string;
  fechaAsamblea?: string;
  condicionesReconexion?: string;
  fechaReconexion?: string;
  reconectado?: boolean;
  dateAdd: string;
}

export interface RenunciaTemporalCrearModel {
  aguaUsuarioId: number;
  fechaRenuncia: string;
  motivo: string;
}

export interface RenunciaTemporalReconexionModel {
  fechaSolicitudReconexion?: string;
  fechaAsamblea?: string;
  condicionesReconexion?: string;
}
