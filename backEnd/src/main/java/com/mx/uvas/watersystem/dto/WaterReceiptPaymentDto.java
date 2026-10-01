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

    // v11 (sept. 2026, pedido explícito de Ely): si esta línea del recibo
    // liquida un cargo pendiente del usuario (Mantenimiento, Aviso, Multa,
    // etc.), aquí viene el id del WaterUserChargeEntity elegido a mano en
    // la pantalla de captura -- selector explícito en vez de que el
    // backend adivine por concepto+año (ver WaterReceiptService.
    // conciliarCargosLiquidados()). Opcional: null si este pago no liquida
    // ningún cargo formal.
    private Integer cargoALiquidarId;
}
