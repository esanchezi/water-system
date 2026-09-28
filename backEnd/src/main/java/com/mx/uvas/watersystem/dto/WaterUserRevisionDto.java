package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WaterUserRevisionDto implements Serializable {
    private Integer revisionId;
    private LocalDate fecha;
    private String tipo;
    private String resultado;
    private String comentario;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer aguaUsuarioId;
}
