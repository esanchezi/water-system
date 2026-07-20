package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

// Una fila del reporte "Totales por año": el año y el total aplicado por
// cada concepto de cobro (llave = catalogoOpcionesId del concepto), más
// egresos y el efectivo que debería haber (totalAnio - egresosAnio).
@Data
public class TotalPorAnioFilaDto implements Serializable {
    private Integer anio;
    private Map<Integer, Double> montosPorConcepto;
    private Double totalAnio;
    private Double egresosAnio;
    private Double efectivoEsperado;
}
