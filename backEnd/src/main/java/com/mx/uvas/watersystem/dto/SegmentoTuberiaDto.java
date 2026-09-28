package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SegmentoTuberiaDto implements Serializable {
    private Integer segmentoId;
    private Integer orden;
    // PVC / POLIDUCTO / MANGUERA / GALVANIZADO / OTRO (texto libre).
    private String material;
    // Texto libre (ej. "1\"", "3/4", "2 pulg").
    private String diametro;
    private BigDecimal longitudMetros;
    private String observaciones;
}
