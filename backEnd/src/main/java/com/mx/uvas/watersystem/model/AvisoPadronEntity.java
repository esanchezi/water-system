package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Aviso para actualización del padrón de habitantes (Art. 5, 15, 15 Bis y
// 15 Ter del Reglamento Interno) ya generado e impreso para un usuario --
// igual que AvisoBombaEntity, NO depende de un cálculo de deuda: aplica por
// igual a cualquier usuario de la calle (se le pide reportar quiénes
// habitan/usan su domicilio), así que aquí tampoco hay periodos adeudados,
// adeudoTotal ni seguimiento de "atendido" -- solo el registro de que se
// generó y, cuando aplica, de su entrega (misma sección "RAZÓN DE
// NOTIFICACIÓN" que el resto de las actas del Comité). Folio propio,
// consecutivo global e independiente del de avisos de adeudo/bomba.
@Entity
@Table(name = "agua_aviso_padron")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AvisoPadronEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950253001L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer avisoPadronId;

    // Consecutivo propio (no comparte secuencia con agua_aviso_adeudo ni
    // agua_aviso_bomba).
    private Integer folioNotificacion;

    // Snapshot de lo impreso -- si el usuario cambia de casa o corrige su
    // nombre después, el histórico sigue mostrando lo que realmente se le
    // entregó.
    private String nombreUsuarioTitular;
    private Integer noCasa;
    private String noCasaTexto;
    private String domicilioToma;

    // "Debe presentarse el día ___ de ___ de 20__" -- mismo campo/criterio
    // que fechaPresentacion en avisos de adeudo (fecha completa elegida al
    // generar, no solo el día).
    private LocalDate fechaPresentacion;

    // 1 = activo, 0 = cancelado (se oculta del historial por default, pero
    // se puede seguir consultando -- mismo patrón que avisos de adeudo/bomba).
    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdCancela;
    private LocalDateTime dateCancela;

    // Registro de entrega -- se llena cuando se marca el aviso como
    // entregado, replicando los mismos datos que se capturan a mano en la
    // sección "RAZÓN DE NOTIFICACIÓN" de la carta física (idéntico a
    // AvisoAdeudoEntity/AvisoBombaEntity).
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
