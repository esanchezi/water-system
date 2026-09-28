package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

// Un tramo (zona de horario) puede estar armado con más de un tipo de
// línea a lo largo de su trazo -- ej. tubería de PVC de 2" al inicio y
// después una manguera de 1" hasta el final -- por eso es una lista aparte,
// en el orden en que van desde el origen (pozo/caja) hacia el final del
// tramo, y no un campo único dentro de TramoEntity.
@Entity
@Table(name = "agua_segmento_tuberia")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SegmentoTuberiaEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251501L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer segmentoId;

    // Orden dentro del tramo (0, 1, 2...), desde el origen hacia el final.
    private Integer orden;

    // PVC / POLIDUCTO / MANGUERA / GALVANIZADO / OTRO -- texto libre, el
    // front sugiere estas opciones pero no está atado a un catálogo fijo
    // (mismo criterio que Valvula.tipo o CajaValvula.lado en esta app).
    private String material;

    // Texto libre para no forzar un solo formato (ej. "1\"", "3/4", "2 pulg").
    private String diametro;

    @Column(precision = 10, scale = 2)
    private BigDecimal longitudMetros;

    private String observaciones;

    @Builder.Default
    private Integer estatus = 1;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramo_id", nullable = false)
    private TramoEntity tramo;
}
