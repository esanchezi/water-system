import { WaterUserModel } from "./WaterUser.model";

export interface AssemblyModel {
  asambleaId: number;
  dateS: string;
  purpose: string;
  observaciones: string;
  attendance: AttendanceModel[];
}

export interface AttendanceModel {
  asambleaAsistenciaId: number;
  observaciones: string;
  asistencia: boolean;
  waterUser: WaterUserModel;
}