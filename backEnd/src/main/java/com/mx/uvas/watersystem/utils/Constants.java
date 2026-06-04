package com.mx.uvas.watersystem.utils;

public final class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    // ==========================
    // Response Codes
    // ==========================
    public static final String OK_RESPONSE_CODE = "00";
    public static final String ERROR_RESPONSE_CODE = "-1";
    public static final String CODIGO_OO = "00";     // ⚠️ Parece duplicado de OK_RESPONSE_CODE
    public static final String CODIGO_MENOS_O1 = "-01"; // ⚠️ Revisa si lo usas distinto a ERROR_RESPONSE_CODE

    // ==========================
    // Response Messages
    // ==========================
    public static final String OK_RESPONSE_MESSAGE = "Respuesta ok";
    public static final String ERROR_RESPONSE_MESSAGE = "Respuesta nok";

    // ==========================
    // Recursos / Entidades
    // ==========================
    public static final String RECIBOS = "Recibos";
    public static final String RECIBOS_HISTORIAL = "Recibos de historial";

    // ==========================
    // Mensajes Comunes
    // ==========================
    public static final String ENCONTRADOS = "encontrados";
    public static final String NO_ENCONTRADOS = "no encontrados";
    public static final String ERROR_AL_CONSULTAR = "Error al consultar";
}
