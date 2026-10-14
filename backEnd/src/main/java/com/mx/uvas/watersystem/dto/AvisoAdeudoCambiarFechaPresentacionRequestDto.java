package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

// Reprogramar el día en que el usuario debe presentarse (ver
// AvisoAdeudoService.cambiarFechaPresentacion()).
@Data
public class AvisoAdeudoCambiarFechaPresentacionRequestDto implements Serializable {
    private LocalDate fechaPresentacion;
    // Motivo del cambio (ej. "no se alcanzó a entregar") -- obligatorio, para
    // que quede constancia.
    private String comentario;
}
