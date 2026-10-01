package com.mx.uvas.watersystem.services.impl;

import java.time.LocalDate;

// Todo lo que necesita un Aviso Informativo de Adeudo individual dentro del
// lote -- ya calculado y listo para imprimir (folio ya asignado). Ver
// AvisoInformativoAdeudoPdfService. El adeudo/multa/mantenimiento se
// calculan igual que en Cartas de adeudo (AdeudoLuzService), NO se capturan
// a mano -- ver AvisoInformativoAdeudoService.generar().
public record CartaInformativoAdeudoDatos(
        Integer folioNotificacion,
        // Ya trae el número de usuario al frente ("577 - Juan Pérez").
        String nombreUsuarioTitular,
        String noCasa,
        String domicilioToma,
        // Texto libre capturado a mano, ej. "2024 - $600.00 | 2025 - $700.00".
        String periodosAdeudados,
        Double multaAcumulada,
        // Desglose de multaAcumulada por concepto -- mismo campo y mismas
        // reglas que CartaAdeudoDatos.multaAcumuladaDesglose (ver
        // AdeudoLuzUsuarioDto.multaAcumuladaDesglose). Null cuando no aplica.
        String multaAcumuladaDesglose,
        // Cooperación extraordinaria de mantenimiento de cajón (Art. 10)
        // pendiente -- se imprime en su propio renglón, aparte de la multa
        // (mismo criterio que Cartas de adeudo v5). mantenimientoPorAnioTexto
        // ya viene formateada ("$100.00 - 2025 | $100.00 - 2026") o "" si no
        // hay nada pendiente.
        Double mantenimientoPendiente,
        String mantenimientoPorAnioTexto,
        Integer noFolioUltimoPago,
        // Ya formateada ("dd/MM/yyyy") o "" si no aplica.
        String fechaUltimoPagoTexto,
        // "Debe presentarse el día ___" -- fecha completa elegida al
        // generar, para poder darle seguimiento (pedido explícito de Ely).
        LocalDate fechaPresentacion,
        // Nota libre del Comité (opcional) -- se imprime como "Nota del
        // Comité: ..." igual que observacionManual en Cartas de adeudo.
        String observacion
) {
}
