package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FamilyMemberDto implements Serializable {
    private Integer familiaIntegranteId;
    private String observaciones;
    private PersonDto person;
    private CatalogOptionsDto tipoFamilia;
}
