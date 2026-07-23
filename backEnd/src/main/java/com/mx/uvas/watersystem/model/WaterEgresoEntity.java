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
// egresoPadre = null) como para cada línea/gasto que lo compone (nivel = 2,
// egresoPadre = la cabecera), e incluso para el desglose interno de una
// línea (nivel = 3+, egresoPadre = esa línea). Es la misma fila para los
// tres casos -- la única diferencia real es a quién apunta `padre_id`
// (columna `padre_id`, mapeada aquí como egresoPadre). El reporte de
// Totales por año solo suma las cabeceras (nivel = 1), así que no se ve
// afectado por esto.
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

    // Fila padre de esta (la cabecera si esta fila es una línea, o la línea
    // si esta fila es un sub-item de desglose). Null si esta fila ES la
    // cabecera del vale. Única columna de parentesco -- ya no existe la
    // columna vieja "egresos_id" que causaba confusión.
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
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
