package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class FeeDto implements Serializable {

    private Integer cuotaId;
    private CatalogOptionsDto uso;
    private CatalogOptionsDto userType;
    private String observaciones;

    // Usados solo al crear/actualizar (el objeto uso/userType es de solo lectura)
    private Integer usoId;
    private Integer userTypeId;

    private List<FeeAmountDto> amount = new ArrayList<>();
}
