package com.mx.uvas.watersystem.services.impl;

import java.time.LocalDate;

// Todo lo que necesita la carta individual de Aviso de Instalación de
// Medidor para imprimirse -- ya calculado y listo (folio ya asignado). Ver
// AvisoMedidorPdfService. A diferencia de CartaBombaDatos, este aviso
// siempre se genera de a uno (cada medidor tiene su propio número de serie
// y lectura inicial).
public record CartaMedidorDatos(
        Integer folioNotificacion,
        // Ya trae el número de usuario al frente ("577 - Juan Pérez") para
        // poder ubicarlo fácil contra el sistema, mismo criterio que el
        // resto de las cartas.
        String nombreUsuarioTitular,
        // "2-D" / "2-I" -- ya trae el lado de la casa cuando se conoce.
        String noCasa,
        String domicilioToma,
        LocalDate fechaInstalacion,
        String noSerieMedidor,
        String lecturaInicialMedidor
) {
}
