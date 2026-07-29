package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

// Censo de personas que habitan con un usuario de agua (integrantes de la
// familia). Se ancla a WaterUserEntity (no a la casa) para que, si el
// usuario cambia de domicilio, el censo lo siga sin tener que reconstruir
// ninguna relación.
//
// La edad es opcional a propósito -- mucha gente no la quiere dar de
// entrada. Para no depender de un proceso manual anual que "actualice" la
// edad (fácil de olvidar), se guarda la edad tal como se capturó junto con
// el año en que se capturó (anioRegistro); la edad actual se calcula sola
// en el mapeo a DTO: edad + (añoActual - anioRegistro).
@Entity
@Table(name = "agua_usuario_censo")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterUserCensusEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer censoId;

    // Nula = "sin clasificar" (persona registrada pero sin dar su edad).
    private Integer edad;

    // Año en que se capturó/actualizó edad -- permite calcular la edad
    // actual sin necesidad de un proceso manual cada año.
    private Integer anioRegistro;

    // Libre, opcional: nombre, parentesco, o cualquier nota (ej. "hijo",
    // "Juan Pérez"). No es obligatorio porque muchos usuarios no darán ni
    // eso -- en el peor de los casos el registro solo cuenta como "una
    // persona más" en el total.
    private String observaciones;

    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agua_usuario_id")
    private WaterUserEntity waterUser;

}
