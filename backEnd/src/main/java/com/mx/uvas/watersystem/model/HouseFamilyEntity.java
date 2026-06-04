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
@Table(name = "casa_familia")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HouseFamilyEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer casaFamiliaId;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @ManyToOne
    @JoinColumn(name = "casa_id", nullable = false)
    private HouseEntity casa;

    @ManyToOne
    @JoinColumn(name = "familia_id", nullable = false)
    private FamilyEntity familia;
}
