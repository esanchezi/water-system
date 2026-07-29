package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WaterUserCensusResumenDto implements Serializable {
    private List<RangoEdadDto> rangos;
    private Integer sinClasificar;
    private Integer totalPersonas;
}
