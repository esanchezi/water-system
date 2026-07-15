package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WaterAgreementChargeDto implements Serializable {
    private Integer aguaConvenioCargoId;
    private Integer aguaConvenioId;
    private Integer aguaUsuarioCargoId;
    private String conceptoCargo;
    private Double montoCargo;
    private Double montoCondonado;
}
