package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Lo que se captura a mano viendo la foto de un recibo -- ver
// ReciboRevisionService.agregar() para cómo se cruza contra agua_recibo.
@Data
public class ReciboRevisionCreateDto implements Serializable {
    private Integer noFolioCapturado;
    private Integer noUsuarioCapturado;
    private String montoTexto;
    private String observaciones;
}
