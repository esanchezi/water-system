package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Actualización parcial de las banderas de control de un vale (nivel = 1).
// Solo se tocan los campos que vengan distintos de null, para poder togglear
// "revisado" y "validadoFisico" de forma independiente desde la vista.
@Data
public class WaterEgresoMarcarDto implements Serializable {
    private Boolean revisado;
    private Boolean validadoFisico;
}
