package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AvisoAdeudoDto implements Serializable {
    private Integer avisoAdeudoId;
    private Integer folioNotificacion;
    private String tipoAviso;
    // PK real del usuario (waterUser.aguaUsuarioId) -- distinto de
    // noUsuario (el número visible/de cuenta). Se usa para poder generar
    // el Segundo aviso directo desde esta fila del historial sin tener que
    // volver a buscar al usuario por su noUsuario (ver botón "Generar
    // Segundo aviso" en el historial / AvisoAdeudoService.candidatoUnico()).
    private Integer aguaUsuarioId;
    private Integer noUsuario;
    private String nombreUsuarioTitular;
    private Integer noCasa;
    private String noCasaTexto;
    private String domicilioToma;
    private String periodosAdeudados;
    private Double adeudoTotal;
    private Double multaAcumulada;
    private Integer noFolioUltimoPago;
    private LocalDateTime fechaUltimoPago;
    // Fecha completa en la que debía presentarse en el Comité, elegida al
    // generar la carta -- ver AvisoAdeudoEntity.fechaPresentacion.
    private LocalDate fechaPresentacion;
    private LocalDateTime dateAdd;

    // Derivado de estatus (0 = cancelada) -- se oculta del historial por
    // default en el frontend, pero se puede consultar explícitamente.
    private Boolean cancelada;
    // Motivo de la cancelación (opcional) -- ej. "no fue entregada, se
    // negaron a recibir" o "no fue entregada, no se encontró a nadie".
    private String comentarioCancela;

    // Derivado de fechaEntrega != null.
    private Boolean entregado;
    private LocalDateTime fechaEntrega;
    // "TITULAR" | "OTRA_PERSONA" | "SE_NEGO" | "NO_ENCONTRADO"
    private String tipoEntrega;
    private String nombreReceptor;
    private String parentescoReceptor;
    private String nombreNotificador;
    private String nombreTestigo1;
    private String nombreTestigo2;
    private String comentarioEntrega;

    // Derivado de fechaAtencion != null -- mientras sea false, la carta ya
    // entregada sigue pendiente del cobro/trámite correspondiente.
    private Boolean atendido;
    private LocalDateTime fechaAtencion;
    // "PAGADO" | "CONDONADO" | "CONVENIO" | "OTRO" -- cómo se resolvió,
    // capturado junto con fechaAtencion. Ver AvisoAdeudoEntity.
    private String resultadoAtencion;
    private String comentarioAtencion;
    // Folio del recibo (agua_recibo) vinculado como comprobante del pago,
    // si se capturó -- solo referencia, el monto real vive en el recibo.
    private Integer folioReciboVinculado;
    // Folio del convenio (agua_convenio) vinculado como referencia, si se
    // capturó -- independiente de folioReciboVinculado, pueden venir ambos.
    private Integer folioConvenioVinculado;
    // Solo cuando resultadoAtencion = CONVENIO: fecha comprometida de pago.
    // Mientras no venza, se pausa el Segundo aviso para este usuario.
    private LocalDate fechaCompromiso;
}
