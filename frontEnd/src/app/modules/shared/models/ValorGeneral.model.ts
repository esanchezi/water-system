// Tabla de valores generales por año -- multa por falta de pago,
// corte/reconexión, aviso, interés moratorio por día, multa de referencia
// por manipular válvulas. Ver ValorGeneralEntity en el backend.
export interface ValorGeneralModel {
  valorGeneralId: number;
  clave: string;
  nombre: string;
  vigencia: number;
  monto: number;
  observaciones?: string;
  activo: boolean;
}

export interface ValorGeneralCrearModel {
  clave: string;
  nombre: string;
  vigencia: number;
  monto: number;
  observaciones?: string;
}

// Claves fijas -- deben coincidir exactamente con ValorGeneralClave.java.
// IMPORTANTE: "nombre" aquí debe escribirse IGUAL a como se llame la
// opción correspondiente en el catálogo CONCEPTO_CARGO_EXTRA (pantalla de
// Catálogos) -- así, al elegir ese concepto en "Nuevo cargo" de la ficha
// del usuario, el sistema sugiere solo el monto vigente configurado aquí
// (ver DetailsUserComponent.onConceptoCargoChange()). Si los nombres no
// coinciden exactamente, simplemente no se sugiere nada (no rompe nada).
export const CLAVES_VALOR_GENERAL: { clave: string; nombre: string }[] = [
  { clave: 'MULTA_FALTA_PAGO', nombre: 'Multa por falta de pago' },
  { clave: 'CORTE_RECONEXION', nombre: 'Corte y reconexión' },
  { clave: 'AVISO', nombre: 'Aviso' },
  { clave: 'INTERES_MORATORIO_DIA', nombre: 'Interés moratorio (por día)' },
  { clave: 'MULTA_VALVULAS', nombre: 'Multa por manipular válvulas' },
];
