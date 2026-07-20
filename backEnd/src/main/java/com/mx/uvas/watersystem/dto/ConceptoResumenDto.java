package com.mx.uvas.watersystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Encabezado de columna del reporte "Totales por año": una opción del
// catálogo "Conceptos de cobro" (catalogo_id = 2).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConceptoResumenDto implements Serializable {
    private Integer catalogoOpcionesId;
    private String nombre;
}
