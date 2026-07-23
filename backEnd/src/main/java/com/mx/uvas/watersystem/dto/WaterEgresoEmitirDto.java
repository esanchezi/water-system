package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

// Petición para juntar varios gastos ya capturados (sueltos) en un solo vale.
@Data
public class WaterEgresoEmitirDto implements Serializable {
    private LocalDate fechaPago;
    private Integer tipoComprobanteId;
    private String noFolio;
    private String descripcion;
    private String justificacion;
    private List<Integer> gastoIds;
}
