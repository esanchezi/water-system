package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

// Qué válvulas hay que abrir/cerrar para que un tramo reciba agua. Es
// muchos a muchos: un tramo involucra varias válvulas, y una misma
// válvula puede aparecer en varios tramos con distinto estado requerido
// (ej. abierta para el tramo de la mañana, cerrada para el de la tarde).
@Entity
@Table(name = "agua_tramo_valvula")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TramoValvulaEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251308L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tramoValvulaId;

    // ABIERTA / CERRADA -- el estado que debe tener la válvula cuando este
    // tramo está en operación.
    private String estadoRequerido;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramo_id", nullable = false)
    private TramoEntity tramo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valvula_id", nullable = false)
    private ValvulaEntity valvula;
}
