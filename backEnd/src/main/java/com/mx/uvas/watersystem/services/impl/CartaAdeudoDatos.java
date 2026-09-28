package com.mx.uvas.watersystem.services.impl;

import java.time.LocalDate;

// Todo lo que necesita una carta individual dentro del lote -- ya
// calculado y listo para imprimir (folio ya asignado). Ver
// AvisoAdeudoPdfService.
public record CartaAdeudoDatos(
        Integer folioNotificacion,
        // "PRIMERO" o "SEGUNDO"
        String tipoAviso,
        // Ya trae el número de usuario al frente ("577 - Juan Pérez") para
        // poder ubicarlo fácil contra el sistema.
        String nombreUsuarioTitular,
        // "2-D" / "2-I" -- ya trae el lado de la casa cuando se conoce.
        String noCasa,
        String domicilioToma,
        Double adeudoTotal,
        Integer noFolioUltimoPago,
        // Ya formateada ("dd/MM/yyyy") o "" si no hay pago previo registrado.
        String fechaUltimoPagoTexto,
        // "2023 - $840.00 | 2024 - $1,200.00 | ... | Total $4,340.00" -- para
        // que el usuario pueda revisar contra su propio cálculo año por año,
        // no solo confiar en el total.
        String desglosePorAnio,
        // Fecha completa en la que debe presentarse en el Comité -- llena
        // el párrafo "Debe presentarse el día ___ de ___ de 20__". Puede
        // venir null si por alguna razón no se mandó (no debería pasar, el
        // frontend lo exige), en cuyo caso el PDF deja el blanco sin llenar.
        LocalDate fechaPresentacion,
        // true si la carta es para una persona SIN usuario en el sistema
        // (ver UsuarioNoRegistradoDto) -- agrega en el PDF la leyenda de
        // "presente sus recibos de un comité anterior", que no aplica a
        // usuarios ya registrados y con historial de pagos en el sistema.
        boolean esNoRegistrado,
        // Suma de cargos manuales pendientes (multa por falta de pago,
        // corte/reconexión, aviso, válvulas -- ver WaterUserChargeEntity /
        // AdeudoLuzService.CONCEPTOS_MULTA_ACUMULADA), NO incluida en
        // adeudoTotal. Imprime la fila "Multa acumulada a la fecha",
        // separada del adeudo de Luz/CFE. Ya incluye el cargo automático de
        // Aviso recién generado para ESTA MISMA carta -- ver
        // AvisoAdeudoService.generar(). YA NO incluye mantenimiento de
        // cajón (v5 de la carta, sept. 2026, ver mantenimientoPendiente más
        // abajo). 0.0 para personas sin usuario registrado (no tienen
        // cargos capturados en el sistema).
        Double multaAcumulada,
        // Solo cuando tipoAviso = "SEGUNDO": texto con cuándo y a quién se
        // le entregó el Primer aviso más reciente de este usuario, para
        // mostrarlo en vez de las notas genéricas de mantenimiento/Art. 22
        // Bis (ver AvisoAdeudoService.construirNotaEntregaPrimerAviso() y
        // AvisoAdeudoPdfService.agregarPaginaAviso()). Null en cualquier
        // otro caso (Primer aviso, o personas sin usuario registrado, que
        // no tienen historial de entregas que consultar).
        String notaEntregaPrimerAviso,
        // Cooperación extraordinaria de mantenimiento de cajón (Art. 10)
        // pendiente de pago -- calculada aparte de multaAcumulada a
        // propósito (v5 de la carta, sept. 2026): se imprime en el mismo
        // renglón que la multa (por espacio) en vez de sumarse a "multa
        // acumulada". 0.0 para personas sin usuario registrado.
        Double mantenimientoPendiente,
        // "$100.00 - 2024 | $100.00 - 2025" -- desglose de
        // mantenimientoPendiente por año, "" si no hay ninguno pendiente.
        String mantenimientoPorAnioTexto
) {
}
