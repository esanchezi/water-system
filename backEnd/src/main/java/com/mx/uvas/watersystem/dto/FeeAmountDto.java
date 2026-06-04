package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FeeAmountDto implements Serializable {
    private Integer cuotaMontoId;
    private Integer cuotaId;
    private Integer cuota;
    private Integer vigencia;

}
