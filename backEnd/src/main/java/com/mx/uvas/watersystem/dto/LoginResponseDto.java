package com.mx.uvas.watersystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private String token;
    private String username;
    private String nombre;
    // ADMIN / USUARIO1 -- el front lo usa para mostrar/ocultar secciones
    // del menú (ver SistemaUsuarioEntity.rol para el detalle).
    private String rol;
}
