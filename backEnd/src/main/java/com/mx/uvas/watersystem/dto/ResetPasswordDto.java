package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Un administrador resetea la contraseña de otra cuenta -- a diferencia de
// "cambiar mi contraseña" (que pide la actual), aquí no aplica porque quien
// resetea no es el dueño de la cuenta.
@Data
public class ResetPasswordDto implements Serializable {
    private String passwordNueva;
}
