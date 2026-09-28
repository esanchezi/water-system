package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RenunciaTemporalDto implements Serializable {
    private Integer renunciaTemporalId;
    private Integer folio;
    private Integer noUsuario;
    private String nombreUsuario;
    private LocalDate fechaRenuncia;
    private String motivo;
    private Double adeudoALaFecha;
    private Boolean cancelada;

    private LocalDate fechaSolicitudReconexion;
    private LocalDate fechaAsamblea;
    private String condicionesReconexion;
    private LocalDateTime fechaReconexion;
    private Boolean reconectado;

    private LocalDateTime dateAdd;
}
