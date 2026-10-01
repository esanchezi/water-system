package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class AvisoInformativoAdeudoGenerarRequestDto implements Serializable {
    private List<AvisoInformativoAdeudoItemRequestDto> usuarios;
    // "Debe presentarse el día ___ de ___ de 20__" -- se pide siempre antes
    // de generar, para poder darle seguimiento (mismo criterio que
    // fechaPresentacion en Cartas de adeudo / Aviso Padron).
    private LocalDate fechaPresentacion;
}
