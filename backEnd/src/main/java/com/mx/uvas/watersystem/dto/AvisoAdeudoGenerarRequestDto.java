package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class AvisoAdeudoGenerarRequestDto implements Serializable {
    private List<Integer> aguaUsuarioIds;
    // "PRIMERO" o "SEGUNDO".
    private String tipoAviso;
    // Fecha completa (día+mes+año) en la que debe presentarse en las
    // oficinas del Comité -- llena el blanco "Debe presentarse el día ___
    // de ___ de 20__" de la carta. Antes solo se pedía el día (1-31) y se
    // asumía siempre el mes/año actual, lo cual salía mal si la carta se
    // generaba a fin de mes para una cita programada el mes siguiente.
    // Obligatorio: el frontend lo pide antes de generar.
    private LocalDate fechaPresentacion;
    // Personas SIN usuario en el sistema (censo en proceso) a las que
    // también se les quiere generar carta -- ver UsuarioNoRegistradoDto.
    // Puede venir null/vacío si el lote es solo de usuarios registrados.
    private List<UsuarioNoRegistradoDto> noRegistrados;
}
