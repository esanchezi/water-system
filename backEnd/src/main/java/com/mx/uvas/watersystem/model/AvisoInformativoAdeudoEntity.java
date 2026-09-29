package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Aviso Informativo de Adeudo: notificación PREVIA y más suave que las
// Cartas de adeudo (agua_aviso_adeudo) -- se manda a mano, solo a ciertos
// usuarios que el Comité elige (no hay cálculo/candidato automático), para
// avisarles de su situación antes de iniciar el proceso formal de avisos y
// suspensión. Folio propio, consecutivo, TOTALMENTE independiente del de
// Cartas de adeudo (ese ya lleva más de 40 folios y no se debe mezclar).
// No tiene seguimiento de "atendido"/cobro ($200 Art. 20 no aplica aquí,
// es puramente informativo) -- solo folio, datos capturados a mano y,
// cuando aplica, el registro de entrega (RAZÓN DE NOTIFICACIÓN).
@Entity
@Table(name = "agua_aviso_informativo_adeudo")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AvisoInformativoAdeudoEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950252101L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer avisoInformativoAdeudoId;

    // Consecutivo propio -- NO comparte secuencia con agua_aviso_adeudo.
    private Integer folioNotificacion;

    // Snapshot de lo impreso, igual que en los demás módulos de carta.
    private String nombreUsuarioTitular;
    private Integer noCasa;
    private String noCasaTexto;
    private String domicilioToma;

    // Todos estos datos se capturan A MANO al generar (selección manual de
    // usuarios, sin cálculo automático -- ver AvisoInformativoAdeudoService),
    // a diferencia de AvisoAdeudoEntity que los calcula con AdeudoLuzService.
    @Column(length = 500)
    private String periodosAdeudados;
    private Double adeudoTotal;
    private Double multaAcumulada;
    private Integer noFolioUltimoPago;
    private LocalDateTime fechaUltimoPago;

    // "Debe presentarse el día ___" -- fecha completa elegida al generar,
    // igual que en AvisoAdeudoEntity/AvisoPadronEntity, para poder darle
    // seguimiento (ver acordeón "Cartas generadas" en la ficha de usuario).
    private LocalDate fechaPresentacion;

    // Nota libre del Comité al momento de elegir a este usuario (opcional).
    @Column(length = 500)
    private String observacion;

    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdCancela;
    private LocalDateTime dateCancela;

    // Registro de entrega -- misma sección "RAZÓN DE NOTIFICACIÓN" que el
    // resto de las cartas del Comité.
    private LocalDateTime fechaEntrega;
    // "TITULAR" | "OTRA_PERSONA" | "SE_NEGO" | "NO_ENCONTRADO"
    private String tipoEntrega;
    private String nombreReceptor;
    private String parentescoReceptor;
    private String nombreNotificador;
    private String nombreTestigo1;
    private String nombreTestigo2;
    @Column(length = 500)
    private String comentarioEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private WaterUserEntity waterUser;
}
