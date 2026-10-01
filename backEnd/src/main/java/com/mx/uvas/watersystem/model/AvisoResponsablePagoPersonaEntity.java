package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

// Cada renglón de la tabla "RESOLUCIÓN DEL COMITÉ (personas y/o familias
// responsables de pago)" de la carta -- hasta N por AvisoResponsablePagoEntity.
// Puede ser una persona SIN usuario dado de alta todavía (solo nombreCompleto
// libre, waterUser null) o, si Ely elige agregar a alguien que ya está
// registrado, se liga a su WaterUserEntity y se guarda un snapshot de la
// cuota vigente que tenía en ese momento (cuotaVigenteSnapshot) -- snapshot y
// no un cálculo en vivo, mismo criterio que el resto de la app (ej. multas
// en AvisoAdeudoEntity), para que el histórico no cambie si después se
// modifica la cuota del usuario.
@Entity
@Table(name = "agua_responsable_pago_persona")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AvisoResponsablePagoPersonaEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950254002L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer responsablePagoPersonaId;

    // Orden de aparición en la tabla (1, 2, 3...) -- la plantilla física
    // numera los renglones.
    private Integer orden;

    private String nombreCompleto;
    // "Relación / parentesco con el titular" -- texto libre (ej. "Titular",
    // "Hijo", "Inquilino"...).
    private String parentesco;
    // "Familia para efectos de cuota (No.)" -- número de grupo/familia
    // dentro del mismo domicilio (Art. 5: el pago es por familia, no por
    // toma; puede haber más de una familia en la misma casa).
    private Integer familiaCuota;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_pago_id", nullable = false)
    private AvisoResponsablePagoEntity responsablePago;

    // Opcional: si esta persona ya está dada de alta como usuario, se liga
    // aquí (autocomplete de usuarios de esta misma casa, ver
    // AvisoResponsablePagoService.usuariosDeLaCasa()). Null si es alguien
    // sin usuario en el sistema todavía.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agua_usuario_id", nullable = true)
    private WaterUserEntity waterUser;

    // Snapshot de la cuota vigente del usuario ligado al momento de
    // agregarlo a esta resolución -- ver comentario de la clase.
    private Double cuotaVigenteSnapshot;
}
