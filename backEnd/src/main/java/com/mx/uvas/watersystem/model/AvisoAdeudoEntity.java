package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Un aviso de adeudo (Primer/Segundo) ya generado e impreso para un
// usuario -- se guarda "congelado" (nombre, domicilio, periodos, adeudo
// tal como se imprimieron) además del folio consecutivo asignado, para
// llevar registro de qué se le entregó a cada quien sin depender de que
// los datos actuales del usuario sigan siendo los mismos después.
// adeudoTotal cubre el adeudo de Luz/CFE (concepto_id = 6) más el interés
// moratorio ya calculado; multaAcumulada es aparte (cargos manuales
// pendientes -- multa, corte/reconexión, aviso, válvulas). No incluye
// mantenimiento ni cooperaciones extraordinarias, tal como aclara la
// propia carta.
@Entity
@Table(name = "agua_aviso_adeudo")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AvisoAdeudoEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251801L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer avisoAdeudoId;

    // Consecutivo global (no por usuario) -- ver AvisoAdeudoService.
    private Integer folioNotificacion;

    // "PRIMERO" o "SEGUNDO".
    private String tipoAviso;

    // Snapshot de lo impreso, tal como se veía al momento de generar la
    // carta -- si el usuario se cambia de casa o corrige su nombre después,
    // el histórico sigue mostrando lo que realmente se le entregó.
    private String nombreUsuarioTitular;
    private Integer noCasa;
    // "2-D" / "2-I" -- noCasa + lado de la casa, para mostrar en el
    // historial igual que se imprimió en la carta.
    private String noCasaTexto;
    private String domicilioToma;
    private String periodosAdeudados;
    private Double adeudoTotal;
    // Suma de cargos manuales pendientes (multa por falta de pago,
    // corte/reconexión, aviso, válvulas) al momento de generar -- snapshot,
    // ver WaterUserChargeEntity / AdeudoLuzService.CONCEPTOS_MULTA_ACUMULADA.
    // 0.0 para personas sin usuario registrado.
    private Double multaAcumulada;
    private Integer noFolioUltimoPago;
    private LocalDateTime fechaUltimoPago;

    // Fecha completa en la que el usuario debía presentarse en el Comité
    // (día+mes+año elegidos al generar, ver AvisoAdeudoService.generar()) --
    // antes solo se usaba para imprimir el párrafo "Debe presentarse el día
    // ___" en el PDF (CartaAdeudoDatos.fechaPresentacion) pero no se
    // guardaba en el historial; se agrega aquí para poder mostrarla en el
    // acordeón "Cartas generadas" de la ficha de usuario.
    private LocalDate fechaPresentacion;

    // 1 = activa, 0 = cancelada (se oculta del historial por default, pero
    // se puede seguir consultando -- ver AvisoAdeudoService.historial()).
    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdCancela;
    private LocalDateTime dateCancela;

    // Registro de entrega -- se llena cuando se marca la carta como
    // entregada, replicando los mismos datos que se capturan a mano en la
    // sección "RAZÓN DE NOTIFICACIÓN" de la carta física.
    private LocalDateTime fechaEntrega;
    // "TITULAR" | "OTRA_PERSONA" | "SE_NEGO" | "NO_ENCONTRADO" -- las 4
    // opciones de la carta física ("marcar lo que corresponda").
    private String tipoEntrega;
    // Solo aplica cuando tipoEntrega = OTRA_PERSONA.
    private String nombreReceptor;
    private String parentescoReceptor;
    private String nombreNotificador;
    private String nombreTestigo1;
    private String nombreTestigo2;
    @Column(length = 500)
    private String comentarioEntrega;

    // Se llena cuando, tras entregar la carta, ya se hizo el cobro (o se
    // atendió de otra forma) correspondiente -- mientras esté en null, el
    // sistema sigue alertando cada vez que se consulta al usuario. Ver
    // AvisoAdeudoService.pendientesDeAtencion()/marcarAtendida().
    private LocalDateTime fechaAtencion;
    private Integer userIdAtencion;
    // "PAGADO" | "CONDONADO" | "CONVENIO" | "OTRO" -- por qué se cerró el
    // seguimiento, para poder filtrar/contar (no solo saber que "ya se
    // atendió", sino cómo se resolvió).
    private String resultadoAtencion;
    // Detalle libre opcional, para aclarar el caso particular (ej. motivo
    // de la condonación) sin perder la categoría de resultadoAtencion.
    private String comentarioAtencion;
    // Folio del recibo (agua_recibo) que corresponde al pago que resolvió
    // este aviso -- solo referencia (no FK), para poder saltar directo al
    // recibo real desde el historial sin duplicar aquí el monto cobrado
    // (ese vive únicamente en el recibo). Ver AvisoAdeudoService.marcarAtendida(),
    // que valida que el folio exista antes de guardarlo.
    private Integer folioReciboVinculado;
    // Folio del convenio (agua_convenio) vinculado como referencia, si
    // aplica -- igual que folioReciboVinculado, es independiente de
    // resultadoAtencion: pueden ir ligados un recibo Y un convenio a la vez
    // (ej. el convenio cubre parte de la deuda y hay un recibo por el
    // resto). El backend valida que el folio exista antes de guardarlo.
    private Integer folioConvenioVinculado;
    // Solo cuando resultadoAtencion = CONVENIO: fecha en la que el usuario
    // se comprometió a pagar (obligatoria en ese caso). Mientras no venza,
    // AvisoAdeudoService pausa el Segundo aviso para este usuario (ya vino
    // a hacer un arreglo); si vence sin que se genere un aviso nuevo,
    // vuelve a requerirlo automáticamente -- no hace falta un convenio
    // formal del módulo de Convenios para esto, aunque uno también cuenta
    // si existe (ver enriquecerConUltimoAviso()).
    private LocalDate fechaCompromiso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private WaterUserEntity waterUser;
}
