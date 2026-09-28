package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PozoDto implements Serializable {
    private Integer pozoId;
    private String nombre;
    private BigDecimal lat;
    private BigDecimal lng;
    private String observaciones;
    private Integer estatus;
}
