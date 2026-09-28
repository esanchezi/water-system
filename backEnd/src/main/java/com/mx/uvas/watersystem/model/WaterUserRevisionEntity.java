package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// Registro de una revisión hecha a un usuario -- puede ser de la toma/
// medidor (física) o de sus datos de padrón, o ambas cosas a la vez
// (campo "tipo"). Es un historial de solo consulta desde la ficha del
// usuario: sirve como evidencia de cuándo se revisó y qué se encontró,
// con fotos de respaldo opcionales (ver WaterUserRevisionFotoEntity).
// El "borrado" es lógico (estatus = 0) para no perder el rastro por un
// error de captura -- mismo criterio que el resto de la app (avisos,
// convenios, etc.), nunca se hace DELETE físico del registro.
@Entity
@Table(name = "agua_usuario_revision")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterUserRevisionEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer revisionId;

    private LocalDate fecha;

    // TOMA_MEDIDOR | DATOS_PADRON | GENERAL -- catálogo fijo y corto, no
    // amerita tabla de catálogo aparte (ver AvisoAdeudo.tipoAviso, mismo
    // criterio de String simple ya usado en la app).
    private String tipo;

    // BIEN | CON_PROBLEMA
    private String resultado;

    @Column(length = 1000)
    private String comentario;

    @Builder.Default
    private Integer estatus = 1;

    private Integer userIdAdd;
    private LocalDateTime dateAdd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private WaterUserEntity waterUser;

    @JsonManagedReference
    @OneToMany(mappedBy = "revision", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<WaterUserRevisionFotoEntity> fotos = new HashSet<>();
}
