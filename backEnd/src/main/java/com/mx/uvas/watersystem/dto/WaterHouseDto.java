package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class WaterHouseDto implements Serializable {

    private Integer casaId;
    private Integer zonaId;
    private Integer casaNo;
    private String nombre;
    private String zona;
    private BigDecimal lat;
    private BigDecimal lng;
    private String observaciones;

    private List<WaterUserDto> listWaterUser;
}
