package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AvisoResponsablePagoDto implements Serializable {
    private Integer responsablePagoId;
    private Integer folioNotificacion;

    private Integer casaId;
    private Integer noCasa;
    private String noCasaTexto;
    private String domicilioToma;

    private String motivoSolicitud;
    private LocalDate fechaSolicitud;
    private String observacionesComite;

    private List<AvisoResponsablePagoPersonaDto> personas;

    private LocalDateTime dateAdd;

    // Derivado de estatus (0 = cancelado) -- se oculta del historial por
    // default en el frontend, pero se puede consultar explícitamente.
    private Boolean cancelado;

    // Derivado de fechaEntrega != null.
    private Boolean entregado;
    private LocalDateTime fechaEntrega;
    private String tipoEntrega;
    private String nombreReceptor;
    private String parentescoReceptor;
    private String nombreNotificador;
    private String nombreTestigo1;
    private String nombreTestigo2;
    private String comentarioEntrega;
}
