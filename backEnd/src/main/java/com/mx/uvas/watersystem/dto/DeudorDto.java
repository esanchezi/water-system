package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeudorDto implements Serializable {

    private Integer aguaUsuarioId;
    private Integer noUsuario;
    private String nombreCompleto;

    private Integer casaId;
    private Integer casaNo;
    private String calleNombre;

    private Integer estatusComiteId;
    private String estatusComiteNombre;

    private Integer anio;
    private Boolean cuotaPendiente;
    private Double montoCuotaAnio;
    private Double montoCuotaPagado;
    private Double montoCuotaPendiente;
    private Double montoCargosPendiente;
    private Double montoTotalPendiente;
}
