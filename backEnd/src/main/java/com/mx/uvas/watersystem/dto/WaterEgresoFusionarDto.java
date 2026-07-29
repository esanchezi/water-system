package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

// Petición para fusionar varios vales YA EMITIDOS (nivel = 1, cada uno con
// su propio folio) en un solo vale nuevo más grande -- ej. el vale de
// nómina de Brandy + el de Francisca, juntos en "Pago de nómina del mes".
// Cada vale seleccionado pasa a ser línea (nivel = 2) del vale nuevo; sus
// propios hijos (sub-líneas, si tenía) no se tocan, solo cambia su padre.
@Data
public class WaterEgresoFusionarDto implements Serializable {
    private LocalDate fechaPago;
    private Integer tipoComprobanteId;
    private String noFolio;
    private String descripcion;
    private String justificacion;
    private List<Integer> valeIds;
}
