package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

// Un año completo del "Resumen anual": nivel 1 del acordeón es el año
// mismo; esta fila trae los datos del nivel 2 (resumen anual) y las dos
// tablas del nivel 3 (mensual y por categoría).
// saldoCaja = inicial + efectivo - egresos (recibosCaja NO entra en esta
// cuenta -- son fondos de Caja Popular, no efectivo disponible de
// inmediato; se muestran solo como referencia, igual que ya hace
// "Totales por año" al considerar nada más tipo_pago = Efectivo).
@Data
public class ResumenAnualFilaDto implements Serializable {
    private Integer anio;
    private Double inicial;
    private Double recibosCaja;
    private Double efectivo;
    private Double egresos;
    private Double saldoCaja;
    private List<ResumenAnualMesDto> meses;
    private List<ResumenAnualCategoriaMesDto> categorias;
}
