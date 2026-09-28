package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ValorGeneralDto implements Serializable {
    private Integer valorGeneralId;
    private String clave;
    private String nombre;
    private Integer vigencia;
    private Double monto;
    private String observaciones;
    private Boolean activo;
}
