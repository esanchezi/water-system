package com.mx.uvas.watersystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WaterUserDetailsDto {
    // Usuario
    private Integer aguaUsuarioId;
    private Integer noUsuario;
    private Boolean habitaDomicilio;
    private Boolean tieneToma;
    private Boolean inmuebleRenta;
    private Integer cuotaId;
    private Integer frecuenciaPagoId;
    private Integer estatusPagoId;
    private Integer estatusComiteId;
    private Integer estatusTomaId;

    // Persona
    private Integer personaId;
    private String nombre;
    private String nombre2;
    private String app;
    private String apm;

    // Dirección
    private Integer direccionId;
    private Integer seccionId;
    private String calle;
    private String numero;
    private String referencia;
    private String entrecalle1;
    private String entrecalle2;
    private String seccionNombre;

    // Casa (opcional)
    private String casaNombre;
    private Integer casaId;

}
