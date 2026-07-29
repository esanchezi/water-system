package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WaterUserCensusResumenDto implements Serializable {
    private List<RangoEdadDto> rangos;
    private Integer sinClasificar;
    private Integer totalPersonas;

    // Mismo total, agrupado por calle/zona de la casa del usuario (Calle
    // Principal, La Barca, San Isidro, Buenavista, etc. -- lo que tengas
    // dado de alta en el catálogo de calles).
    private List<RangoEdadDto> porZona;
    private Integer sinZonaAsignada;
}
