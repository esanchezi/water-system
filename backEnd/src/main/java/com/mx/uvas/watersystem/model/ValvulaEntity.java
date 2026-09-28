package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "agua_valvula")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ValvulaEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251304L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer valvulaId;
    private String identificador;
    private String tipo;
    // Estado operativo: ABIERTA / CERRADA
    private String estado;
    private String observaciones;
    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false)
    private CajaValvulaEntity caja;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonManagedReference
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            mappedBy = "valvula"
    )
    private List<ValvulaFotoEntity> fotos;
}
