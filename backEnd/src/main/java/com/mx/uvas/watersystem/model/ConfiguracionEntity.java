package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

// Ajustes generales de la app (nombre del comité, bloqueo de
// configuraciones, etc.) guardados como llave/valor -- así se pueden
// agregar más ajustes después (logo, moneda...) sin tener que cambiar el
// esquema de la tabla cada vez.
@Entity
@Table(name = "agua_configuracion")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ConfiguracionEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251401L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer configuracionId;

    @Column(unique = true, nullable = false)
    private String clave;

    private String valor;
    private String descripcion;

    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;
}
