package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "agua_caja_valvula")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CajaValvulaEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251303L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cajaId;
    private String codigo;
    private String nombre;
    @Column(precision = 38, scale = 13)
    private BigDecimal lat;
    @Column(precision = 38, scale = 13)
    private BigDecimal lng;
    private String observaciones;

    // Lado de la calle donde está la caja (IZQUIERDO / DERECHO), solo para
    // calles con 2 redes independientes (ej. la principal) -- ayuda a
    // agrupar/filtrar en el front para no ver todas las cajas de un tramo
    // mezcladas. Opcional: no todas las calles lo necesitan.
    private String lado;

    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    // Liga opcional a un tramo, solo para reportes -- la caja existe como
    // inventario físico independientemente de que se le asigne un tramo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramo_id", nullable = true)
    private TramoEntity tramo;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "caja"
    )
    private Set<ValvulaEntity> listValvula;
}
