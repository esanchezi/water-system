package com.mx.uvas.watersystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CambiarPasswordDto {
    @NotBlank
    private String passwordActual;
    @NotBlank
    private String passwordNueva;
}
