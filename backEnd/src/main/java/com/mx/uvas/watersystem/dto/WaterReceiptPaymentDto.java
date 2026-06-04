package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WaterReceiptPaymentDto implements Serializable {
    private Integer aguaReciboPagoId;
    private LocalDateTime fechaPago;
    private String fechaPagoStr;
    private Double montoRecibido;
    private Double montoAplicado;
    private Integer anio;
    private Integer reciboId;
    private Integer comiteId;
    private Integer tipoPagoId;
    private Integer conceptoId;
    private String concepto;
}
