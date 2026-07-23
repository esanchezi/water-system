package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class WaterEgresoDto implements Serializable {
    private Integer aguaEgresosId;
    private LocalDate fechaPago;
    private String fechaPagoStr;
    private LocalDate fechaCompra;
    private String fechaCompraStr;
    private Double monto;
    private String descripcion;
    private String proveedor;
    private String noFolio;
    private String justificacion;
    private Integer tipoComprobanteId;
    private String tipoComprobanteNombre;
    private Integer personaId;
    private String personaNombre;

    // Concepto directo de la cabecera: solo aplica a registros históricos
    // (previos al diseño de líneas por categoría), donde el egreso completo
    // era de una sola categoría. Los vales nuevos categorizan por línea y
    // dejan esto en null.
    private Integer conceptoId;
    private String conceptoNombre;

    // Banderas de control para la limpieza histórica: solo aplican a nivel=1
    // (el vale en sí, no sus líneas). "revisado" = ya se checó que el vale
    // esté bien capturado/agrupado; "validadoFisico" = ya se confirmó contra
    // el papel/recibo físico correspondiente.
    private Boolean revisado;
    private Boolean validadoFisico;

    private List<WaterEgresoLineaDto> lineas;
}
