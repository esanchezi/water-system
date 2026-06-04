package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AssemblyAttendanceDto implements Serializable {
    private Integer asambleaAsistenciaId;
    private Integer asambleaId;
    private Integer usuarioId;
    private String observaciones;
    private Boolean asistencia;
    private WaterUserDto waterUser;
}
