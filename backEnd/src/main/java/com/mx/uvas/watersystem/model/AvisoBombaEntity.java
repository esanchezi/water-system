package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Aviso por uso indebido de bomba (Art. 23 del Reglamento Interno) ya
// generado e impreso para un usuario -- a diferencia de los avisos de
// adeudo, este NO depende de un cálculo de deuda: aplica por igual a
// cualquier usuario de la calle (reporte de uso de bomba fuera de lo
// autorizado), así que aquí no hay periodos adeudados, adeudoTotal ni
// seguimiento de "atendido" -- solo el registro de que se generó y, cuando
// aplica, de su entrega (misma sección "RAZÓN DE NOTIFICACIÓN" que el resto
// de las actas del Comité). Folio propio, consecutivo global e
// independiente del de avisos de adeudo (mismo patrón que
// RenunciaTemporalEntity.folio).
@Entity
@Table(name = "agua_aviso_bomba")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AvisoBombaEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950252001L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer avisoBombaId;

    // Consecutivo propio (no comparte secuencia con agua_aviso_adeudo).
    private Integer folioNotificacion;

    // Snapshot de lo impreso -- si el usuario cambia de casa o corrige su
    // nombre después, el histórico sigue mostrando lo que realmente se le
    // entregó.
    private String nombreUsuarioTitular;
    private Integer noCasa;
    private String noCasaTexto;
    private String domicilioToma;

    // "Fecha del reporte / revisión" de la carta física -- cuándo se
    // recibió el reporte de uso indebido, no cuándo se imprime/entrega.
    private LocalDate fechaReporte;

    // 1 = activo, 0 = cancelado (se oculta del historial por default, pero
    // se puede seguir consultando -- mismo patrón que avisos de adeudo).
    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdCancela;
    private LocalDateTime dateCancela;

    // Registro de entrega -- se llena cuando se marca el aviso como
    // entregado, replicando los mismos datos que se capturan a mano en la
    // sección "RAZÓN DE NOTIFICACIÓN" de la carta física (idéntico a
    // AvisoAdeudoEntity).
    private LocalDateTime fechaEntrega;
    // "TITULAR" | "OTRA_PERSONA" | "SE_NEGO" | "NO_ENCONTRADO"
    private String tipoEntrega;
    private String nombreReceptor;
    private String parentescoReceptor;
    private String nombreNotificador;
    private String nombreTestigo1;
    private String nombreTestigo2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private WaterUserEntity waterUser;
}
