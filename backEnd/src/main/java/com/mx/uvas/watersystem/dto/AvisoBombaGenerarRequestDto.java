package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class AvisoBombaGenerarRequestDto implements Serializable {
    private List<Integer> aguaUsuarioIds;
    // "Fecha del reporte / revisión" que se llena en la carta -- se pide
    // siempre antes de generar, igual que fechaPresentacion en avisos de
    // adeudo.
    private LocalDate fechaReporte;
}
