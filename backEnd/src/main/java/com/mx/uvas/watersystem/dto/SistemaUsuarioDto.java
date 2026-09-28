package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Cuenta de acceso (login), sin datos sensibles -- nunca lleva el hash de
// la contraseña. Para crear/editar la contraseña se usan endpoints aparte.
@Data
public class SistemaUsuarioDto implements Serializable {
    private Integer sistemaUsuarioId;
    private String username;
    private String nombre;
    // ADMIN / USUARIO1 -- texto libre (ver comentario en la entidad).
    private String rol;
    private Integer estatus;
}
