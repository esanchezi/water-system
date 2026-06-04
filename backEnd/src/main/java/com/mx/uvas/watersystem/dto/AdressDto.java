package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdressDto implements Serializable {
    private Integer direccionId;
    private Integer seccionId;
    private Integer noCasa;
    private String calle;
    private String numero;
    private String referencia;
    private String entrecalle1;
    private String entrecalle2;
    private String coordenadas;

    private CatalogOptionsDto seccion;
}
