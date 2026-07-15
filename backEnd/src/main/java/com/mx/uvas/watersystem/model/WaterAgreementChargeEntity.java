package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

// Línea de un convenio: cuánto se condona de un cargo específico.
@Entity
@Table(name = "agua_convenio_cargo")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterAgreementChargeEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aguaConvenioCargoId;
    private Double montoCondonado;
    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agua_convenio_id")
    private WaterAgreementEntity convenio;

    // Dueño de la relación hacia el cargo condonado. No se cascadea desde
    // WaterUserChargeEntity: el convenio es quien controla el ciclo de vida
    // de esta línea, el cargo solo la lee para calcular su saldo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agua_usuario_cargo_id")
    private WaterUserChargeEntity cargo;

}
