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

// Convenio de pago: acuerdo entre el comité y el usuario que puede
// condonar una o varias líneas de agua_usuario_cargo.
@Entity
@Table(name = "agua_convenio")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterAgreementEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aguaConvenioId;
    // Folio del talonario foliado, cuando se usa uno (no todos los
    // convenios llevan folio, por eso es opcional/nullable).
    private Integer noFolio;
    private LocalDate fecha;
    private String motivo;
    private String comentario;
    // Monto adeudado al momento de firmar el convenio (referencia, no
    // necesariamente igual a la suma de los cargos condonados).
    private Double adeudo;
    // Fecha en que el usuario se compromete a liquidar/pagar.
    private LocalDate fechaCompromisoPago;
    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agua_usuario_id")
    private WaterUserEntity waterUser;

    @OneToOne
    @JoinColumn(name = "estatus_convenio_id", nullable = true)
    private CatalogOptionsEntity estatusConvenio;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonManagedReference
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "convenio"
    )
    private Set<WaterAgreementChargeEntity> cargos;

    public void addCargo(WaterAgreementChargeEntity linea) {
        if (Objects.isNull(this.cargos)) this.cargos = new HashSet<>();
        this.cargos.add(linea);
    }

    @Transient
    public double getMontoCondonadoTotal() {
        if (Objects.isNull(this.cargos)) return 0d;
        return this.cargos.stream()
                .filter(c -> c.getEstatus() != null && c.getEstatus() == 1)
                .mapToDouble(WaterAgreementChargeEntity::getMontoCondonado)
                .sum();
    }

}
