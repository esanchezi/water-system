package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegistroSuministroTramoDto implements Serializable {
    private Integer registroTramoId;
    private Integer orden;
    private Integer tramoId;
    private String tramoNombre;
}
