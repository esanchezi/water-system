package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "agua_usuario_cargo_pago")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterUserChargePaymentEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aguaUsuarioCargoPagoId;
    private Double montoAplicado;
    private LocalDateTime fechaPago;
    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agua_usuario_cargo_id")
    private WaterUserChargeEntity cargo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recibo_id")
    private WaterReceiptEntity waterReceipt;

}
