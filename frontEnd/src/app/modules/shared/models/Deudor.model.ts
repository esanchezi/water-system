export interface DeudorModel {
  aguaUsuarioId: number;
  noUsuario: number;
  nombreCompleto: string;

  casaId: number | null;
  casaNo: number | null;
  calleNombre: string | null;

  estatusComiteId: number | null;
  estatusComiteNombre: string | null;

  anio: number;
  cuotaPendiente: boolean;
  montoCuotaAnio: number;
  montoCuotaPagado: number;
  montoCuotaPendiente: number;
  montoCargosPendiente: number;
  montoTotalPendiente: number;
}
