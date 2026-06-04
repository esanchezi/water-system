package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "familia_integrante")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FamilyMemberEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer familiaIntegranteId;
    private String observaciones;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @OneToOne
    @JoinColumn(name="familia_id", nullable=true)
    private FamilyEntity familia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "persona_id")
    private PersonEntity person;

    @OneToOne
    @JoinColumn(name="tipo_persona_id", nullable=true)
    private CatalogOptionsEntity catTipo;

}
