package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WaterUserCensusDto implements Serializable {

    private Integer censoId;
    private Integer edad;
    private Integer anioRegistro;
    private String observaciones;
    private Integer estatus;

    // Calculada al vuelo (no se guarda): edad + (añoActual - anioRegistro).
    // Null si no se capturó edad ("sin clasificar").
    private Integer edadActual;
}
