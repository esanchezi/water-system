package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class RenunciaTemporalCrearRequestDto implements Serializable {
    private Integer aguaUsuarioId;
    private LocalDate fechaRenuncia;
    private String motivo;
}
