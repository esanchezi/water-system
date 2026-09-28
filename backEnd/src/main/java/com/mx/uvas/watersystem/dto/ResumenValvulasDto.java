package com.mx.uvas.watersystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Resumen: total de cajas de válvulas y total de válvulas en todas las cajas.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenValvulasDto implements Serializable {
    private Long totalCajas;
    private Long totalValvulas;
}
