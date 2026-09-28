package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;

@Data
public class TramoHorarioDto implements Serializable {
    private Integer horarioId;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer pozoId;
    private String pozoNombre;
}
