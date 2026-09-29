package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Aviso de resolución sobre personas responsables de pago de un domicilio
// (Art. 5, 15, 15 Bis y 15 Ter del Reglamento Interno). A diferencia de las
// demás cartas (Adeudo, Bomba, Padrón), esta NO se liga a un WaterUserEntity
// -- se liga directo a la Casa (WaterHouseEntity), porque el motivo de la
// carta es justamente resolver quiénes viven/usan el domicilio y quién paga,
// independientemente de si ya están o no dados de alta como usuario (pueden
// no estarlo todavía). Las personas involucradas viven en la lista hija
// AvisoResponsablePagoPersonaEntity. Folio propio, consecutivo, independiente
// del de las demás cartas.
@Entity
@Table(name = "agua_responsable_pago")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AvisoResponsablePagoEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950254001L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer responsablePagoId;

    // Consecutivo propio (no comparte secuencia con las demás cartas).
    private Integer folioNotificacion;

    // Snapshot de lo impreso -- si la casa cambia de calle/número después,
    // el histórico sigue mostrando lo que realmente se le entregó.
    private Integer noCasa;
    private String noCasaTexto;
    private String domicilioToma;

    // Motivo por el que se solicitó/generó esta resolución (ej. "Solicitud
    // del usuario titular", "Desacuerdo entre habitantes", "Detección de
    // negocio no reportado"...) y cuándo se recibió esa solicitud -- llenado
    // libre por el Comité, ver el formulario de generación.
    private String motivoSolicitud;
    private LocalDate fechaSolicitud;

    // "Observaciones adicionales del Comité" -- texto libre, mismo campo que
    // trae la plantilla física.
    private String observacionesComite;

    // 1 = activo, 0 = cancelado (se oculta del historial por default, pero
    // se puede seguir consultando -- mismo patrón que las demás cartas).
    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdCancela;
    private LocalDateTime dateCancela;

    // Registro de entrega -- mismos campos que AvisoAdeudoEntity/
    // AvisoBombaEntity/AvisoPadronEntity, misma sección "RAZÓN DE
    // NOTIFICACIÓN" de la carta física.
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
    @JoinColumn(name = "casa_id", nullable = false)
    private WaterHouseEntity casa;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "responsablePago"
    )
    @Builder.Default
    private List<AvisoResponsablePagoPersonaEntity> personas = new java.util.ArrayList<>();

    public void addPersona(AvisoResponsablePagoPersonaEntity persona) {
        if (this.personas == null) {
            this.personas = new java.util.ArrayList<>();
        }
        persona.setResponsablePago(this);
        this.personas.add(persona);
    }
}
