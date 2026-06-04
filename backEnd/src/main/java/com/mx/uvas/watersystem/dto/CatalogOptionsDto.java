package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CatalogOptionsDto implements Serializable {
    private Integer catalogoOpcionesId;
    private String nombre;
    private String descripcion;

}
