package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// Candidato para carta de adeudo: usuario con saldo pendiente de la
// aportación de Luz/CFE (concepto_id = 6) en uno o más años -- ver
// AdeudoLuzService.
@Data
public class AdeudoLuzUsuarioDto implements Serializable {
    private Integer aguaUsuarioId;
    private Integer noUsuario;
    private String nombreCompleto;
    private Integer casaNo;
    // "2-D" / "2-I" -- casaNo + lado (Derecho/Izquierdo) de la casa, cuando
    // se tiene capturado. casaNo se conserva aparte (Integer) para poder
    // seguir ordenando por número de casa.
    private String casaNoTexto;
    private String calleNombre;
    private String domicilioToma;

    // Estatus del Comité (catálogo) -- para identificar de un vistazo a
    // quiénes ya tienen corte del servicio, igual que ya se muestra en el
    // módulo de Deudores.
    private Integer estatusComiteId;
    private String estatusComiteNombre;
    private List<Integer> periodosAdeudados;
    private String periodosAdeudadosTexto;
    private Double adeudoTotal;

    // Último pago de Luz registrado (cualquier año) -- para "No. de recibo
    // del último pago" / "Fecha del último pago" en la carta.
    private Integer noFolioUltimoPago;
    private LocalDateTime fechaUltimoPago;

    // Control de Primer/Segundo aviso -- último aviso ACTIVO (no cancelado)
    // que se le generó a este usuario, para saber si ya le toca el
    // siguiente. Ver AvisoAdeudoService.enriquecerConUltimoAviso().
    // "PRIMERO" o "SEGUNDO", null si nunca se le ha generado ninguno.
    private String ultimoTipoAviso;
    private Boolean ultimoAvisoEntregado;
    private LocalDateTime ultimoAvisoFechaEntrega;
    // true si el último aviso fue PRIMERO y ya se entregó -- sigue
    // apareciendo como candidato (todavía debe), así que lo que falta es
    // generarle el Segundo aviso, no repetir el Primero. Se pone en false
    // mientras enConvenioVigente sea true (ver más abajo), aunque por lo
    // demás ya le tocaría.
    private Boolean requiereSegundoAviso;

    // true si el usuario tiene un convenio activo (agua_convenio) cuya
    // fecha comprometida de pago todavía no vence -- mientras esté vigente,
    // se pausa el Segundo aviso (ya vino a hacer un arreglo con el comité,
    // no se le debe empujar el siguiente aviso mientras cumple lo
    // acordado). fechaCompromisoConvenio es la fecha comprometida de ese
    // convenio, para mostrarla en pantalla.
    private Boolean enConvenioVigente;
    private LocalDate fechaCompromisoConvenio;

    // Interés moratorio (Art. 22) ya incluido dentro de adeudoTotal -- se
    // expone aparte solo para que se pueda mostrar el desglose (Luz +
    // interés = adeudo total) sin tener que recalcularlo en el frontend.
    private Double interesMoratorio;

    // Suma de cargos manuales pendientes de pago (multa por falta de pago,
    // corte/reconexión, aviso, multa de válvulas -- ver WaterUserChargeEntity
    // / AdeudoLuzService.CONCEPTOS_MULTA_ACUMULADA), NO incluida en
    // adeudoTotal. Es la fila "Multa acumulada a la fecha" que se imprime
    // aparte en la carta de adeudo.
    private Double multaAcumulada;

    // Cooperación extraordinaria de mantenimiento de cajón (Art. 10)
    // pendiente de pago -- calculada aparte de multaAcumulada a propósito
    // (v5 de la carta, sept. 2026): ya no se suma a "multa acumulada", se
    // imprime junto en el mismo renglón de la carta pero con su propio
    // desglose (ver mantenimientoPorAnioTexto).
    private Double mantenimientoPendiente;

    // Desglose de mantenimientoPendiente por año (ej. {2025: 100.0}) --
    // solo los años que de verdad tienen un cargo activo, no
    // necesariamente los 2 de ANIOS_MANTENIMIENTO, para poder ver de un
    // vistazo cuál año falta si el usuario cree que debería tener los dos.
    // Se guarda como mapa (no ya formateado a texto) para poder combinarlo
    // fácil con los cargos que se creen justo al generar la carta -- ver
    // AvisoAdeudoService.generar().
    private Map<Integer, Double> mantenimientoPorAnio;
}
