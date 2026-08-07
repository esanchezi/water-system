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
    // Si es negocio (en vez de domicilio habitacional) -- ayuda a entender
    // el uso del pozo/consumo. El giro (abarrotes, papelería, etc.) es
    // opcional y viene de catálogo, no está fijo en código, para no
    // amarrarnos a una lista cerrada de tipos de negocio.
    private Boolean esNegocio;
    // Solo aplica cuando esNegocio = true: si opera en un local (espacio
    // físico dedicado) o no (ej. vende desde la misma casa sin local
    // aparte), y si ese local lo renta el mismo usuario -- ambos datos
    // ayudan a decidir la cuota (ver reglas de negocio en curso).
    private Boolean tieneLocal;
    private Boolean localRentadoPorUsuario;

    // Clasificación para la calculadora de cuota SUGERIDA (ver
    // WaterUserService/frontend: nunca cambia la cuota sola, solo propone
    // una categoría en base a estas respuestas -- la decisión final la
    // toma la persona capturando).
    private Boolean esTiendaAbarrotes;         // dominio negocio
    private Boolean negocioAtendidoPorUsuario; // dominio negocio
    private Boolean negocioGrande;             // dominio negocio
    // Apodo/alias por el que se conoce al usuario -- hay usuarios que es
    // más fácil ubicar por alias que por su nombre completo. Opcional,
    // se incluye en las búsquedas junto con el nombre.
    private String alias;
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
    private FeeEntity fee;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "giro_negocio_id", nullable = true)
    private CatalogOptionsEntity giroNegocio;

    // Tipo de usuario -- catálogo TIPO_USUARIO (familia, viuda/o, casa
    // deshabitada, toma sin conectar, etc.). Reemplaza los antiguos
    // booleans familiaCompleta/viudoPadreMadreSoltero: el catálogo ya
    // cubre esos casos y varios más sin necesitar código nuevo cada vez
    // que se agrega una clasificación.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "tipo_usuario_id", nullable = true)
    private CatalogOptionsEntity tipoUsuario;

}
