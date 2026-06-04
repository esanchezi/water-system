package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agua_recibo_historial")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterReceiptHistoryEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aguaReciboHistorialId;
    private String noFolio;
    private LocalDate fecha;
    private String nombre;
    private String calle;
    private String numero;
    private String colonia;
    private String concepto;
    private Double monto;
    private String observaciones;
    private Boolean esProcesado;
    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agua_usuario_id" , nullable=true)
    private WaterUserEntity waterUser;

    @OneToOne
    @JoinColumn(name="comite_id", nullable=true)
    private CatalogOptionsEntity comite;

}
