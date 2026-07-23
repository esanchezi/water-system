package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Fila de la tabla mensual dentro del acordeón de un año: cuánto entró
// (por tipo de pago) y cuánto salió ese mes. totalMes = recibosCaja + efectivo.
@Data
public class ResumenAnualMesDto implements Serializable {
    private Integer mes;
    private String mesNombre;
    private Double totalMes;
    private Double recibosCaja;
    private Double efectivo;
    private Double egresos;
}
