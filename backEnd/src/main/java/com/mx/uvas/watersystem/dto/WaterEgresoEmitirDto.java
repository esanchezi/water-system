package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

// Petición para juntar en un solo vale: gastos sueltos ya capturados
// (gastoIds, nivel = 2 sin padre) y/o vales que ya se habían emitido por
// separado (valeIds, nivel = 1 -- ej. el vale de nómina de Brandy que se
// va a incluir junto con los gastos sueltos de Francisca y Elizabeth en
// "Pago de nómina de mayo"). Los vales incluidos bajan a nivel 2 (pasan a
// ser línea del vale nuevo); sus propias sub-líneas no se tocan.
// conceptoId es opcional: si todo el vale es de una sola categoría (ej.
// Nómina), se puede asignar directo a la cabecera para que no aparezca
// "sin categoría" en los reportes que agrupan por concepto de la cabecera.
@Data
public class WaterEgresoEmitirDto implements Serializable {
    private LocalDate fechaPago;
    private Integer tipoComprobanteId;
    private String noFolio;
    private String descripcion;
    private String justificacion;
    private Integer conceptoId;
    private List<Integer> gastoIds;
    private List<Integer> valeIds;
}
