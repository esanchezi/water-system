package com.mx.uvas.watersystem.services.impl;

import java.time.LocalDate;

// Todo lo que necesita una carta individual de Aviso por uso indebido de
// bomba dentro del lote -- ya calculado y listo para imprimir (folio ya
// asignado). Ver AvisoBombaPdfService.
public record CartaBombaDatos(
        Integer folioNotificacion,
        // Ya trae el número de usuario al frente ("577 - Juan Pérez") para
        // poder ubicarlo fácil contra el sistema, mismo criterio que la
        // carta de adeudo.
        String nombreUsuarioTitular,
        // "2-D" / "2-I" -- ya trae el lado de la casa cuando se conoce.
        String noCasa,
        String domicilioToma,
        // "Fecha del reporte / revisión" que se llena en la tabla de datos.
        LocalDate fechaReporte
) {
}
