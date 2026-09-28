package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agua_pozo")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PozoEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251301L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pozoId;
    private String nombre;
    @Column(precision = 38, scale = 13)
    private BigDecimal lat;
    @Column(precision = 38, scale = 13)
    private BigDecimal lng;
    private String observaciones;
    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;
}
