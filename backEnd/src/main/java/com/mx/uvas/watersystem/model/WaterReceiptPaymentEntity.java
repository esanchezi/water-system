package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "agua_recibo_pago")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterReceiptPaymentEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aguaReciboPagoId;
    private LocalDateTime fechaPago;
    private Double montoRecibido;
    private Double montoAplicado;
    private Integer anio;
    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recibo_id")
    private WaterReceiptEntity waterReceipt;

    @OneToOne
    @JoinColumn(name="comite_id", nullable=false)
    private CatalogOptionsEntity catComite;

    @OneToOne
    @JoinColumn(name="concepto_id", nullable=true)
    private CatalogOptionsEntity catConcepto;

    @OneToOne
    @JoinColumn(name="tipo_pago_id", nullable=true)
    private CatalogOptionsEntity catTiPag;

    public void addReceipt(WaterReceiptEntity receipt){
        this.waterReceipt = receipt;
    }

    /*public void  removeReceipt(Integer id){
        this.waterReceipt
    }*/

}
