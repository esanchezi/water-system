package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RenunciaTemporalReconexionRequestDto implements Serializable {
    private LocalDate fechaSolicitudReconexion;
    private LocalDate fechaAsamblea;
    private String condicionesReconexion;
}
