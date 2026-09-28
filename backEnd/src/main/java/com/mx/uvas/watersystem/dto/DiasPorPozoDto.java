package com.mx.uvas.watersystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Resumen: cuántos días (registros) tiene cada pozo, para responder
// "cuántos días tienen agua y de qué pozo".
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiasPorPozoDto implements Serializable {
    private Integer pozoId;
    private String pozoNombre;
    private Long totalDias;
}
