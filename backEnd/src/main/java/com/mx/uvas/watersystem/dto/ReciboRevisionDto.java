package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReciboRevisionDto implements Serializable {
    private Integer revisionId;
    private Integer fotoId;

    // Lo que se capturó a mano viendo el papel.
    private Integer noFolioCapturado;
    private Integer noUsuarioCapturado;
    private String montoTexto;
    private String observaciones;
    private String resultado;
    private LocalDateTime dateAdd;

    // Lo que dice el sistema para el recibo que hizo match (null si no
    // se encontró ninguno) -- se pinta al lado de lo capturado para
    // comparar a simple vista.
    private Integer reciboId;
    private Integer noFolioSistema;
    private Integer noUsuarioSistema;
    private String nombreUsuarioSistema;
    private Double montoSistema;
    private String conceptoSistema;
    private LocalDate fechaSistema;
}
