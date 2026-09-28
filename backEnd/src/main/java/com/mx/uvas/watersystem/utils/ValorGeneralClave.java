package com.mx.uvas.watersystem.utils;

// Claves fijas de la tabla de valores generales (agua_valor_general) --
// ver ValorGeneralEntity. INTERES_MORATORIO_DIA se usa para el cálculo
// automático de interés (ver AdeudoLuzService). Las demás (multa por falta
// de pago, corte/reconexión, aviso, multa de válvulas) son solo montos de
// referencia para cuando el Comité registra un cargo manual -- el cargo en
// sí se registra en el sistema de "Cargos/Multas" ya existente por usuario
// (WaterUserChargeEntity, catálogo CONCEPTO_CARGO_EXTRA), no aquí; ver
// AvisoAdeudoService.MULTA_ACUMULADA_CONCEPTOS para cómo se relacionan por
// nombre con las opciones de ese catálogo.
public final class ValorGeneralClave {

    private ValorGeneralClave() {
        throw new IllegalStateException("Utility class");
    }

    public static final String MULTA_FALTA_PAGO = "MULTA_FALTA_PAGO";
    public static final String CORTE_RECONEXION = "CORTE_RECONEXION";
    public static final String AVISO = "AVISO";
    public static final String INTERES_MORATORIO_DIA = "INTERES_MORATORIO_DIA";
    public static final String MULTA_VALVULAS = "MULTA_VALVULAS";
    // A diferencia de las demás, esta se consulta con getMontoVigente(clave,
    // año) usando el AÑO QUE SE ESTÁ COBRANDO (2024 o 2025), no el año
    // actual -- ver AvisoAdeudoService.crearCargoMantenimiento().
    public static final String MANTENIMIENTO = "MANTENIMIENTO";
}
