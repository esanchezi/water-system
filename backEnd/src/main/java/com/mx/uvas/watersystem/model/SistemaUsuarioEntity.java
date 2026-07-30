package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

// Cuenta para entrar al sistema (login) -- NO confundir con WaterUserEntity
// (que es el "usuario de agua"/toma de servicio, es decir un cliente del
// comité, no una cuenta de acceso al software).
//
// Independiente de PersonEntity a propósito: quien entra al sistema no
// necesariamente corresponde a un registro de persona/usuario de agua (por
// ejemplo, la administradora del sistema). Reemplaza el intento anterior con
// Keycloak -- este login es autocontenido (usuario/contraseña en esta misma
// base de datos), sin depender de un servicio externo aparte que había que
// mantener corriendo.
@Entity
@Table(name = "sistema_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SistemaUsuarioEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sistemaUsuarioId;

    @Column(unique = true, nullable = false)
    private String username;

    // Hash (BCrypt), nunca la contraseña en claro.
    private String passwordHash;

    private String nombre;

    // Por ahora solo "ADMIN" -- se deja como texto libre (no catálogo) para
    // no sobre-construir un sistema de roles que todavía no se necesita
    // (un solo usuario admin por ahora).
    private String rol;

    private Integer estatus;
    private LocalDateTime dateAdd;
    private LocalDateTime dateUpdate;
}
