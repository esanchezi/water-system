package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class WaterUserAnnualPaymentDto implements Serializable {

    private Integer pagoAnualId;
    private Integer anio;
    private LocalDate fechaValidacion;
    private String observaciones;
    private Integer estatus;
}
