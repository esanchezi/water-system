package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

// Tabla de valores generales parametrizables por año -- multa por falta de
// pago, corte/reconexión, aviso, interés moratorio por día, multa de
// referencia por manipular válvulas. Mismo espíritu que cuota/cuota_monto
// (FeeEntity/FeeAmountEntity) pero simplificado: aquí no hay uso/tipo de
// usuario, solo un concepto fijo (ver ValorGeneralClave) + su monto vigente
// por año, para que el Comité pueda ajustar montos sin depender de un
// redeploy. Un mismo "clave" puede tener varios renglones, uno por año
// (vigencia) -- se usa el más reciente <= año consultado como "vigente".
@Entity
@Table(name = "agua_valor_general")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ValorGeneralEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer valorGeneralId;

    // Ver ValorGeneralClave: MULTA_FALTA_PAGO, CORTE_RECONEXION, AVISO,
    // INTERES_MORATORIO_DIA, MULTA_VALVULAS.
    private String clave;
    // Nombre descriptivo para mostrar en pantalla (ej. "Multa por falta de pago").
    private String nombre;
    private Integer vigencia;
    private Double monto;
    private String observaciones;

    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;
}
