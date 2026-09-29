package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AvisoInformativoAdeudoFotoDto implements Serializable {
    private Integer fotoId;
    private String nombreArchivo;
    private String nombreOriginal;
    private String contentType;
    private Integer avisoInformativoAdeudoId;
    private LocalDateTime dateAdd;
}
