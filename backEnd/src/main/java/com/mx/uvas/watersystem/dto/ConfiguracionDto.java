package com.mx.uvas.watersystem.dto;

import lombok.Data;

@Data
public class ConfiguracionDto {
    private Integer configuracionId;
    private String clave;
    private String valor;
    private String descripcion;
    private Integer estatus;
}
