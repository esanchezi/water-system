package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "agua_tramo")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TramoEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251302L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tramoId;
    private String nombre;
    private String descripcion;

    // Lado de la calle (IZQUIERDO / DERECHO), para calles con 2 redes
    // independientes -- ej. la principal, donde cada lado tiene su propio
    // horario y por eso se maneja como 2 tramos separados. Opcional: la
    // mayoría de las calles solo tienen una red y no lo necesitan.
    private String lado;

    // Trazo del tramo sobre el mapa: arreglo JSON de puntos
    // [{"lat":21.04,"lng":-101.57}, ...]. Puede venir de Directions
    // (siguiendo la calle real) o de un dibujo manual punto por punto --
    // el front decide cómo interpretarlo, aquí solo se guarda tal cual.
    @Lob
    @Column(columnDefinition = "TEXT")
    private String trazoJson;

    // DIRECTIONS o MANUAL -- solo informativo, mientras se decide cuál
    // método de trazo se queda como definitivo.
    private String metodoTrazo;

    // Un mismo tramo puede tener más de un horario (ej. "Jazmín" -> jueves
    // matutino del pozo Buenavista, Y domingo vespertino del mismo pozo).
    // Por eso es una lista, no campos únicos.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "tramo"
    )
    private List<TramoHorarioEntity> horarios;

    // Qué válvulas hay que abrir/cerrar para que este tramo reciba agua.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "tramo"
    )
    private List<TramoValvulaEntity> valvulas;

    // Segmentos de tubería/manguera que forman este tramo, en el orden en
    // que van desde el origen -- un mismo tramo puede combinar más de un
    // material/diámetro a lo largo de su trazo.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "tramo"
    )
    private List<SegmentoTuberiaEntity> segmentos;

    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;
}
