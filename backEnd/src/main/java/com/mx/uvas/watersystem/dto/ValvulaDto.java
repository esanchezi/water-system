package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ValvulaDto implements Serializable {
    private Integer valvulaId;
    private String identificador;
    private String tipo;
    private String estado;
    private String observaciones;
    private Integer estatus;
    private Integer cajaId;
    // Fotos de esta válvula -- solo lectura aquí (se suben/borran con sus
    // propios endpoints, no como parte de guardar la caja/válvula).
    private List<ValvulaFotoDto> fotos;
}
