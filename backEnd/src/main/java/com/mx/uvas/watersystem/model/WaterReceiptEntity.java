package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "agua_recibo")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterReceiptEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aguaReciboId;
    private Integer noFolio;
    private LocalDate fecha;
    private String observaciones;
    private String concepto;
    private Double total;
    private Integer estatus;
    // Marca de invalidación del recibo completo (no confundir con "estatus").
    // NULL = recibo válido; con valor = recibo invalidado y no debe contarse
    // en reportes de totales.
    private Integer invalido;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private WaterUserEntity waterUser;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "waterReceipt"
    )
    private Set<WaterReceiptPaymentEntity> waterReceiptPayment;

    @OneToOne
    @JoinColumn(name="concepto_id", nullable=true)
    private CatalogOptionsEntity catConcepto;

    public void addPayment(WaterReceiptPaymentEntity paymentEntity){
        if((Objects.isNull(this.waterReceiptPayment))) this.waterReceiptPayment = new HashSet<>();
        this.waterReceiptPayment.add(paymentEntity);
    }

    public void removePayment(Integer id){
        if((Objects.isNull(this.waterReceiptPayment))) this.waterReceiptPayment = new HashSet<>();
        this.waterReceiptPayment.removeIf(payment->payment.getAguaReciboPagoId().equals(id));
    }
    public void updatePayments(){
        if((Objects.isNull(this.waterReceiptPayment))) this.waterReceiptPayment = new HashSet<>();
        this.waterReceiptPayment.forEach(payment -> payment.setWaterReceipt(this));
    }
}
