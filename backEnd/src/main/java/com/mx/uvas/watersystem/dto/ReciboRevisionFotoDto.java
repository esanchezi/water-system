package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReciboRevisionFotoDto implements Serializable {
    private Integer fotoId;
    private String nombreArchivo;
    private String nombreOriginal;
    private String contentType;
    private String observaciones;
    private LocalDateTime dateAdd;
    private List<ReciboRevisionDto> revisiones;
}
