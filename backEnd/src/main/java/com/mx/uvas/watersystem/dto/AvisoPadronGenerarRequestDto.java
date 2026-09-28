package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class AvisoPadronGenerarRequestDto implements Serializable {
    private List<Integer> aguaUsuarioIds;
    // "Debe presentarse el día ___ de ___ de 20__" -- se pide siempre antes
    // de generar, mismo criterio que fechaPresentacion en avisos de adeudo.
    private LocalDate fechaPresentacion;
}
