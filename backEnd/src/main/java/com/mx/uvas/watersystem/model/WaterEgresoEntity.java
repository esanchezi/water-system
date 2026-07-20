package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "agua_egresos")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterEgresoEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1905122041950251208L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aguaEgresosId;
    private Double monto;
    private LocalDate fechaPago;
    private Integer estatus;
    private Integer nivel;
}
