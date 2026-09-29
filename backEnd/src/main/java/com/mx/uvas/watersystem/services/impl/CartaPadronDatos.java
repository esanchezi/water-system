package com.mx.uvas.watersystem.services.impl;

import java.time.LocalDate;

// Todo lo que necesita una carta individual de Aviso para actualización del
// padrón dentro del lote -- ya calculado y listo para imprimir (folio ya
// asignado). Ver AvisoPadronPdfService.
public record CartaPadronDatos(
        Integer folioNotificacion,
        // Ya trae el número de usuario al frente ("577 - Juan Pérez") para
        // poder ubicarlo fácil contra el sistema, mismo criterio que las
        // demás cartas.
        String nombreUsuarioTitular,
        // "2-D" / "2-I" -- ya trae el lado de la casa cuando se conoce.
        String noCasa,
        String domicilioToma,
        // "Debe presentarse el día ___ de ___ de 20__" -- fecha completa
        // elegida al generar (mismo criterio que en avisos de adeudo).
        LocalDate fechaPresentacion,
        // Motivo de la solicitud -- opcional, null/"" si no se capturó.
        String motivoSolicitud
) {
}
