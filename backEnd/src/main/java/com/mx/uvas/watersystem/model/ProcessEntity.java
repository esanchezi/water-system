package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tramite")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProcessEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tramiteId;
    private Date fechaEntrega;
    private String observaciones;
    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "persona_id")
    private PersonEntity person;

    @OneToOne
    @JoinColumn(name="tipo_tramite_id", nullable=true)
    private CatalogOptionsEntity catTipo;

    @OneToOne
    @JoinColumn(name="estatus_tramite_id", nullable=true)
    private CatalogOptionsEntity catEstatus;

}
