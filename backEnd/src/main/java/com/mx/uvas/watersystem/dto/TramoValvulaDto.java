package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TramoValvulaDto implements Serializable {
    private Integer tramoValvulaId;
    // ABIERTA / CERRADA
    private String estadoRequerido;
    private Integer valvulaId;
    private String valvulaIdentificador;
    private Integer cajaId;
    private String cajaNombre;
    private String cajaLado;
}
