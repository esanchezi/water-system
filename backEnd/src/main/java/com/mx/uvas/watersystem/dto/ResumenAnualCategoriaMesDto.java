package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Desglose de egresos del mes en las dos categorías principales (mismo
// criterio que el acordeón de Egresos: "Luz" es su propia columna,
// "Mantenimiento" agrupa todo lo demás). luz + mantenimiento = egresos del
// mes en ResumenAnualMesDto.
@Data
public class ResumenAnualCategoriaMesDto implements Serializable {
    private Integer mes;
    private String mesNombre;
    private Double luz;
    private Double mantenimiento;
}
