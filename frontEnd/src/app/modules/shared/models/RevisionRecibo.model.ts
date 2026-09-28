// Revisión de recibos de papel contra lo que ya está capturado en el
// sistema -- ver ReciboRevisionEntity/ReciboRevisionFotoEntity en el
// backend. No hay lectura automática (OCR) de nada: folio y usuario se
// capturan a mano viendo la foto, y el sistema busca y compara.

export interface ReciboRevisionModel {
  revisionId?: number;
  fotoId?: number;

  // Lo que dice el papel, capturado a mano.
  noFolioCapturado: number | null;
  noUsuarioCapturado: number | null;
  montoTexto?: string;
  observaciones?: string;
  resultado?: string;
  dateAdd?: string;

  // Lo que dice el sistema para el recibo que hizo match (undefined si no
  // se encontró ninguno con ese folio).
  reciboId?: number;
  noFolioSistema?: number;
  noUsuarioSistema?: number;
  nombreUsuarioSistema?: string;
  montoSistema?: number;
  conceptoSistema?: string;
  fechaSistema?: string;
}

export interface ReciboRevisionFotoModel {
  fotoId?: number;
  nombreArchivo?: string;
  nombreOriginal?: string;
  contentType?: string;
  observaciones?: string;
  dateAdd?: string;
  revisiones?: ReciboRevisionModel[];
}

export interface ResultadoRevisionInfo {
  valor: string;
  etiqueta: string;
  color: string;
}

export const RESULTADOS_REVISION: ResultadoRevisionInfo[] = [
  { valor: 'PENDIENTE', etiqueta: 'Pendiente', color: '#9e9e9e' },
  { valor: 'COINCIDE', etiqueta: 'Coincide', color: '#2e7d32' },
  { valor: 'DISCREPANCIA', etiqueta: 'Discrepancia', color: '#c62828' },
  { valor: 'NO_ENCONTRADO', etiqueta: 'No encontrado', color: '#ef6c00' },
];

export function infoResultado(resultado: string | undefined): ResultadoRevisionInfo {
  return RESULTADOS_REVISION.find(r => r.valor === resultado) ?? RESULTADOS_REVISION[0];
}
