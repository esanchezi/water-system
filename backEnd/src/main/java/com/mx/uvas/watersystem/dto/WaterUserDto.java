package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WaterUserDto implements Serializable {
    private Integer aguaUsuarioId;
    private Integer cuotaId;
    private Integer frecuenciaPagoId;
    private Integer estatusPagoId;
    private Integer estatusComiteId;
    private Integer estatusTomaId;
    private Integer noUsuario;
    private Integer casaNo;
    private Integer grupoId;
    private Boolean habitaDomicilio;
    private Boolean tieneToma;
    private Boolean inmuebleRenta;
    private String email;
    private String observaciones;
    private FeeDto fee;
    private CatalogOptionsDto estatusPago;
    private CatalogOptionsDto estatusComite;
    private CatalogOptionsDto frecuenciaPago;
    private CatalogOptionsDto estatusToma;
    private PersonDto person;
    private AdressDto adress;
}
