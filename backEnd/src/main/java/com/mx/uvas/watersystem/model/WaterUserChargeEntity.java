package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "agua_usuario_cargo")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterUserChargeEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aguaUsuarioCargoId;
    private String descripcion;
    private Double monto;
    private LocalDate fecha;
    private String comentario;

    // Solo se captura a mano cuando aplica (ej. multas por manipular
    // válvulas o reconexión no autorizada) -- ver
    // WaterUserChargeHelper.buildEntity(). Cuando aprobadoAsamblea es true,
    // la carta de adeudo imprime "(aprobado por asamblea el dd/mm/aaaa)" en
    // el desglose de "Multa acumulada" junto a este cargo -- ver
    // AdeudoLuzService (multaAcumuladaDesglose). fechaAsamblea puede quedar
    // en null aunque aprobadoAsamblea sea true (ej. se sabe que se aprobó
    // pero no se capturó la fecha exacta de esa asamblea), en cuyo caso la
    // carta imprime la leyenda sin fecha.
    private Boolean aprobadoAsamblea;
    private LocalDate fechaAsamblea;

    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agua_usuario_id")
    private WaterUserEntity waterUser;

    @OneToOne
    @JoinColumn(name = "concepto_id", nullable = false)
    private CatalogOptionsEntity concepto;

    // Un cargo se puede liquidar en uno o varios abonos, cada uno ligado a
    // su propio recibo (mismo patrón que agua_recibo / agua_recibo_pago).
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonManagedReference
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "cargo"
    )
    private Set<WaterUserChargePaymentEntity> pagos;

    // Solo lectura: qué líneas de convenio condonaron parte de este cargo.
    // El dueño de esta relación es WaterAgreementChargeEntity (no se cascadea
    // aquí) para evitar que dos padres distintos intenten controlar el mismo
    // ciclo de vida.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "cargo")
    private Set<WaterAgreementChargeEntity> condonaciones;

    public void addPago(WaterUserChargePaymentEntity pago) {
        if (Objects.isNull(this.pagos)) this.pagos = new HashSet<>();
        this.pagos.add(pago);
    }

    @Transient
    public double getMontoPagado() {
        if (Objects.isNull(this.pagos)) return 0d;
        return this.pagos.stream()
                .filter(p -> p.getEstatus() != null && p.getEstatus() == 1)
                .mapToDouble(WaterUserChargePaymentEntity::getMontoAplicado)
                .sum();
    }

    @Transient
    public double getMontoCondonado() {
        if (Objects.isNull(this.condonaciones)) return 0d;
        return this.condonaciones.stream()
                .filter(c -> c.getEstatus() != null && c.getEstatus() == 1)
                .mapToDouble(WaterAgreementChargeEntity::getMontoCondonado)
                .sum();
    }

    @Transient
    public double getSaldo() {
        double monto = this.monto != null ? this.monto : 0d;
        return monto - getMontoPagado() - getMontoCondonado();
    }

    @Transient
    public boolean isPagado() {
        return getSaldo() <= 0;
    }

}
