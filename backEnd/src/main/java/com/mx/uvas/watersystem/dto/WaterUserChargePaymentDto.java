package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class WaterUserChargePaymentDto implements Serializable {
    private Integer aguaUsuarioCargoPagoId;
    private Integer aguaUsuarioCargoId;
    private Integer reciboId;
    private Integer noFolio;
    private Double montoAplicado;
    private LocalDateTime fechaPago;
    private String fechaPagoStr;
}
