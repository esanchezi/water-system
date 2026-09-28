package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;

// Un tramo puede tener más de un horario (ej. "Jazmín" recibe agua los
// jueves en la mañana del pozo Buenavista, Y los domingos en la tarde del
// mismo pozo) -- por eso el horario es una lista aparte, no campos únicos
// dentro de TramoEntity.
@Entity
@Table(name = "agua_tramo_horario")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TramoHorarioEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251307L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer horarioId;

    // LUNES / MARTES / MIERCOLES / JUEVES / VIERNES / SABADO / DOMINGO
    private String diaSemana;

    // Rango de horas en el que le toca agua a este tramo (ej. 06:00 a 10:00).
    private LocalTime horaInicio;
    private LocalTime horaFin;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramo_id", nullable = false)
    private TramoEntity tramo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pozo_id", nullable = false)
    private PozoEntity pozo;
}
