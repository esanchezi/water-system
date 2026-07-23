package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// agua_egresos se usa tanto para la cabecera de un vale/recibo (nivel = 1,
// fkIdEgresos = null) como para cada línea de categoría que compone ese vale
// (nivel = 2, fkIdEgresos = id de la cabecera). El reporte de Totales por año
// solo suma las cabeceras (nivel = 1), así que no se ve afectado por esto.
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

    private Boolean valido;
    private Double monto;
    private LocalDate fechaPago;
    private LocalDate fechaCompra;
    private String descripcion;
    private String proveedor;
    private String noFolio;
    private String justificacion;
    private Integer estatus;
    private Integer nivel;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    // Control de limpieza histórica -- solo aplican al vale (nivel = 1), no
    // a sus líneas. "revisado": ya se checó que el vale esté bien capturado
    // y agrupado. "validadoFisico": ya se confirmó contra el papel/recibo.
    private Boolean revisado;
    private Boolean validadoFisico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concepto_id")
    private CatalogOptionsEntity concepto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_comprobante_id")
    private CatalogOptionsEntity tipoComprobante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private PersonEntity persona;

    // Cabecera del vale al que pertenece esta línea (null si esta fila ES la
    // cabecera).
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_egresos")
    private WaterEgresoEntity egresoPadre;

    // Líneas de categoría de este vale (solo aplica cuando esta fila es la
    // cabecera).
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonManagedReference
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            mappedBy = "egresoPadre"
    )
    private List<WaterEgresoEntity> lineas;
}
