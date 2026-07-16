package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "agua_casa")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterHouseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer casaId;
    private Integer casaNo;
    private String nombre;
    @Column(precision = 38, scale = 13)
    private BigDecimal lat;
    @Column(precision = 38, scale = 13)
    private BigDecimal lng;
    private String observaciones;
    private String lado;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @OneToOne
    @JoinColumn(name="calle_id", nullable=true)
    private CatalogOptionsEntity catCalle;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "waterHouse"
    )
    private Set<WaterUserEntity> listWaterUser;

}
