package com.mx.uvas.watersystem.services.impl;

import java.time.LocalDate;
import java.util.List;

// Todo lo que necesita la carta de "Aviso sobre personas responsables de
// pago del domicilio" -- ya lista para imprimir (folio ya asignado). A
// diferencia de las demás cartas, no hay "nombreUsuarioTitular" porque esta
// carta se genera por Casa, no por usuario (puede haber varias personas, o
// ninguna todavía dada de alta) -- quiénes son los responsables es
// justamente lo que resuelve la tabla "personas". Ver
// AvisoResponsablePagoPdfService.
public record CartaResponsablePagoDatos(
        Integer folioNotificacion,
        // "2-D" / "2-I" -- ya trae el lado de la casa cuando se conoce.
        String noCasa,
        String domicilioToma,
        String motivoSolicitud,
        LocalDate fechaSolicitud,
        List<CartaResponsablePagoPersona> personas,
        String observacionesComite
) {
    // Un renglón de la tabla "RESOLUCIÓN DEL COMITÉ".
    public record CartaResponsablePagoPersona(
            Integer orden,
            String nombreCompleto,
            String parentesco,
            Integer familiaCuota
    ) {
    }
}
