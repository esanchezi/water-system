package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Solicitud y acta de renuncia temporal al servicio de agua potable
// (Art. 6 Bis del reglamento) -- mientras dure (fechaReconexion == null),
// no se le genera cuota ni se le considera candidato a carta de adeudo
// (ver AdeudoLuzService), pero sus adeudos previos siguen vigentes
// (adeudoALaFecha es solo el snapshot de lo que debía al momento de
// renunciar, para constancia -- no exime nada). El propio registro de esta
// tabla ES el "estatus" del usuario (no se usa el catálogo de estatus del
// Comité) -- así no depende de que se dé de alta un catálogo nuevo.
@Entity
@Table(name = "agua_renuncia_temporal")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RenunciaTemporalEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer renunciaTemporalId;

    // Consecutivo global, mismo patrón que folioNotificacion en avisos de adeudo.
    private Integer folio;
    private LocalDate fechaRenuncia;
    private String motivo;
    // Snapshot de lo que el usuario debía (Luz + interés) al momento de
    // renunciar -- constancia por escrito, no afecta el cálculo real.
    private Double adeudoALaFecha;

    // 1 = activa, 0 = cancelada (se dio de alta por error) -- mismo patrón
    // de baja suave que el resto del sistema.
    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdCancela;
    private LocalDateTime dateCancela;

    // Se llenan cuando el usuario pide reconectarse -- mientras
    // fechaReconexion sea null, el usuario sigue "en renuncia temporal".
    private LocalDate fechaSolicitudReconexion;
    private LocalDate fechaAsamblea;
    private String condicionesReconexion;
    private LocalDateTime fechaReconexion;
    private Integer userIdReconexion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private WaterUserEntity waterUser;
}
