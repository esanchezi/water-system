package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class WaterReceiptDto implements Serializable {
    private Integer aguaReciboId;
    private Integer noFolio;
    private LocalDate fecha;
    private String observaciones;
    private String concepto;
    private Double total;
    private Integer usuarioId;
    private Integer conceptoId;
    private String fechaStr;
    private List<WaterReceiptPaymentDto> waterReceiptPayment;
    private WaterUserDto waterUser;
    private List<Integer> aniosPagados;
}
