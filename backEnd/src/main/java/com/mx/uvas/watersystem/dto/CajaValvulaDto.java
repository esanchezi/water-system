package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CajaValvulaDto implements Serializable {
    private Integer cajaId;
    private String codigo;
    private String nombre;
    private BigDecimal lat;
    private BigDecimal lng;
    private String observaciones;
    private String lado;
    private Integer estatus;
    private Integer tramoId;
    private String tramoNombre;
    private List<ValvulaDto> listValvula;
}
