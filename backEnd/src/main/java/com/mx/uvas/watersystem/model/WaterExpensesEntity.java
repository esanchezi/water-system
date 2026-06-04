package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agua_egresos")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterExpensesEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aguaEgresosId;
    private LocalDate fechaPago;
    private LocalDate fechaCompra;
    private Double monto;
    private String descripcion;
    private String proveedor;
    private String noFolio;
    private String justificacion;
    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @OneToOne
    @JoinColumn(name="concepto_id", nullable=true)
    private CatalogOptionsEntity catConcepto;

    @OneToOne
    @JoinColumn(name="tipo_comprobante_id", nullable=true)
    private CatalogOptionsEntity catComprobante;

    @OneToOne
    @JoinColumn(name="persona_id", nullable=true)
    private PersonEntity persona;
}
