package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Payload para dar de alta una cuenta de acceso nueva -- separado de
// SistemaUsuarioDto porque este sí lleva la contraseña en claro (viaja una
// sola vez, por HTTPS/localhost, y se hashea de inmediato en el backend;
// nunca se guarda ni se regresa tal cual).
@Data
public class SistemaUsuarioCreateDto implements Serializable {
    private String username;
    private String password;
    private String nombre;
    private String rol;
}
