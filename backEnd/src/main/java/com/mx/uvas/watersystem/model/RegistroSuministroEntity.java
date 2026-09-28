package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "agua_registro_suministro")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RegistroSuministroEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251305L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer registroId;
    private LocalDate fecha;

    // MATUTINO / VESPERTINO
    private String turno;
    private String observaciones;

    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pozo_id", nullable = false)
    private PozoEntity pozo;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "registro"
    )
    @OrderBy("orden ASC")
    private List<RegistroSuministroTramoEntity> listTramos;
}
