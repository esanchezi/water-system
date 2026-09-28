package com.mx.uvas.watersystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Resumen: cuántos días ha recibido agua cada tramo (y de qué pozo más
// reciente), para saber por ejemplo "15 de Agosto" cuántos días ha tenido agua.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiasPorTramoDto implements Serializable {
    private Integer tramoId;
    private String tramoNombre;
    private Long totalDias;
}
