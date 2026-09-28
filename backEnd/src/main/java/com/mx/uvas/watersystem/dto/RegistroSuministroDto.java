package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class RegistroSuministroDto implements Serializable {
    private Integer registroId;
    private LocalDate fecha;
    // MATUTINO / VESPERTINO
    private String turno;
    private String observaciones;
    private Integer estatus;
    private Integer pozoId;
    private String pozoNombre;
    private List<RegistroSuministroTramoDto> listTramos;
}
