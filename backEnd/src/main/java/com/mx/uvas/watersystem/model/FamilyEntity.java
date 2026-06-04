package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "familia")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FamilyEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer familiaId;
    private String observaciones;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @OneToOne
    @JoinColumn(name="clasificacion_id", nullable=true)
    private CatalogOptionsEntity clasificacionFamily;

    @OneToMany(mappedBy = "familia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FamilyMemberEntity> members;

}
