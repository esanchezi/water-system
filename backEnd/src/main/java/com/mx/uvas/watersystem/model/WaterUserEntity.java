package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "agua_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterUserEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aguaUsuarioId;
    private Integer noUsuario;
    private Boolean habitaDomicilio;
    private Boolean tieneToma;
    private Boolean inmuebleRenta;
    private String email;
    private String observaciones;
    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "persona_id")
    private PersonEntity person;

    @ManyToOne
    @JoinColumn(name = "direccion_id")
    private AdressEntity address;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "waterUser"
    )
    private Set<WaterReceiptEntity> waterReceipt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "waterUser"
    )
    private Set<WaterUserNoticeEntity> waterNotice;

    @OneToOne
    @JoinColumn(name="cuota_id", nullable=true)
    private FeeAmountEntity feeAmount;

    @OneToOne
    @JoinColumn(name="frecuencia_pago_id", nullable=true)
    private CatalogOptionsEntity frecuenciaPago;

    @OneToOne
    @JoinColumn(name="estatus_pago_id", nullable=true)
    private CatalogOptionsEntity estatusPago;

    @OneToOne
    @JoinColumn(name="estatus_comites_id", nullable=true)
    private CatalogOptionsEntity estatusComite;

    @OneToOne
    @JoinColumn(name="estatus_toma", nullable=true)
    private CatalogOptionsEntity estatusToma;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "casa_id", nullable = true)
    private WaterHouseEntity waterHouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "grupo_id", nullable = true)
    private WaterGroupEntity waterGroup;

}
