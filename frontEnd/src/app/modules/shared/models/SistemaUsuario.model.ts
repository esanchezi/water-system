// Cuenta de acceso (login) -- NO confundir con WaterUserModel (el cliente
// del servicio de agua). Roles disponibles por ahora: ADMIN (acceso
// completo) y USUARIO1 (sin las secciones de Finanzas).
export interface SistemaUsuarioModel {
  sistemaUsuarioId?: number;
  username: string;
  nombre?: string;
  rol: string;
  estatus?: number;
}

export interface SistemaUsuarioCreateModel {
  username: string;
  password: string;
  nombre?: string;
  rol: string;
}

export const ROLES_SISTEMA = ['ADMIN', 'USUARIO1'];
