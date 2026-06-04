package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class CatalogDto implements Serializable {

    private Integer catalogoId;
    private String nombre;
    private String descripcion;

    private List<CatalogOptionsDto> options = new ArrayList<>();
}
