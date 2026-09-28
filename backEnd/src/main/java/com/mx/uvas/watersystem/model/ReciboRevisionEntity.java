package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

// Una fila = un recibo de papel capturado a mano mientras se ve la foto
// (ReciboRevisionFotoEntity puede tener varias). No se lee nada
// automáticamente de la imagen -- folio y usuario se capturan a mano y el
// sistema busca y compara contra lo que ya existe en agua_recibo, para
// detectar recibos mal capturados (usuario equivocado, folio inexistente,
// etc.) sin depender de OCR sobre letra manuscrita.
@Entity
@Table(name = "agua_recibo_revision")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ReciboRevisionEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251702L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer revisionId;

    // Lo que dice el papel, tal como se capturó.
    private Integer noFolioCapturado;
    private Integer noUsuarioCapturado;
    // Monto tal como aparece en letra en el recibo -- solo texto libre de
    // referencia, para comparar a ojo contra el monto real del sistema
    // (nunca se compara automático, ahí es donde más se puede equivocar
    // la letra a mano).
    private String montoTexto;
    private String observaciones;

    // PENDIENTE / COINCIDE / DISCREPANCIA / NO_ENCONTRADO -- ver
    // ResultadoRevisionRecibo en el frontend para las etiquetas.
    private String resultado;

    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foto_id", nullable = false)
    private ReciboRevisionFotoEntity foto;

    // Recibo del sistema que hizo match con el folio/usuario capturado
    // (null si no se encontró ninguno con ese folio ni ese usuario).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recibo_id", nullable = true)
    private WaterReceiptEntity waterReceipt;
}
