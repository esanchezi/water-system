package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "agua_registro_suministro_tramo")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RegistroSuministroTramoEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251306L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer registroTramoId;

    // Orden del tramo dentro del recorrido de ese turno (ej. 1 = "15 de
    // Agosto", 2 = "Principal Buenavista", 3 = "Principal Los López").
    private Integer orden;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registro_id", nullable = false)
    private RegistroSuministroEntity registro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramo_id", nullable = false)
    private TramoEntity tramo;
}
