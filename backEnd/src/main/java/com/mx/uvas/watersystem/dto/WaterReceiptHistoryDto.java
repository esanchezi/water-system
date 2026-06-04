package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class WaterReceiptHistoryDto implements Serializable {
    private Integer aguaReciboHistorialId;
    private Boolean esProcesado;
    private String noFolio;
    private LocalDate fecha;
    private String fechaStr;
    private String nombre;
    private String calle;
    private String numero;
    private String colonia;
    private String concepto;
    private String observaciones;
    private Double monto;
    private Integer anio;
    private Integer aguaUsuarioId;
    private WaterUserDto waterUser;
    private CatalogOptionsDto comite;
}
