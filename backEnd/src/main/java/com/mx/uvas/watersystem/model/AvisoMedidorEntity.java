package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Aviso de Instalación de Medidor (prueba de consumo, Art. 7 del Reglamento
// Interno) -- a diferencia de Cartas de adeudo, aquí NO hay Primero/Segundo
// aviso: es un solo documento por instalación, y NO depende de ningún
// cálculo de adeudo/candidatos -- el domicilio de prueba se elige a mano
// (el Comité decide en qué casa instalar el medidor de prueba). Folio
// propio, consecutivo e independiente del resto de avisos (mismo patrón
// que AvisoBombaEntity/RenunciaTemporalEntity).
@Entity
@Table(name = "agua_aviso_medidor")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AvisoMedidorEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950252101L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer avisoMedidorId;

    // Consecutivo propio (no comparte secuencia con las demás cartas).
    private Integer folioNotificacion;

    // Snapshot de lo impreso -- si el usuario cambia de casa o corrige su
    // nombre después, el histórico sigue mostrando lo que realmente se le
    // entregó.
    private String nombreUsuarioTitular;
    private Integer noCasa;
    private String noCasaTexto;
    private String domicilioToma;

    // Datos propios de esta carta (prueba de consumo) -- se capturan al
    // generar, no dependen de ningún cálculo.
    private LocalDate fechaInstalacion;
    private String noSerieMedidor;
    // Texto libre a propósito (puede traer ceros a la izquierda o unidades,
    // ej. "000123" o "123.5 m3") -- no es un valor sobre el que se calcule
    // nada en el sistema, solo se imprime y se archiva.
    private String lecturaInicialMedidor;

    // 1 = activo, 0 = cancelado (se oculta del historial por default, pero
    // se puede seguir consultando -- mismo patrón que el resto de avisos).
    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdCancela;
    private LocalDateTime dateCancela;

    // Registro de entrega -- se llena cuando se marca el aviso como
    // entregado, replicando los mismos datos que se capturan a mano en la
    // sección "RAZÓN DE NOTIFICACIÓN" de la carta física (idéntico al resto
    // de los avisos del Comité).
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
