package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProcessDto implements Serializable {
    private Integer tramiteId;
    private String observaciones;
    private String fechaEntrega;
    private PersonDto person;
    private CatalogOptionsDto tipoTramite;
    private CatalogOptionsDto estatusTramite;
}
