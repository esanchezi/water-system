package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AvisoBombaDto implements Serializable {
    private Integer avisoBombaId;
    private Integer folioNotificacion;

    private Integer aguaUsuarioId;
    private Integer noUsuario;
    private String nombreUsuarioTitular;
    private Integer noCasa;
    private String noCasaTexto;
    private String domicilioToma;

    private LocalDate fechaReporte;
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
