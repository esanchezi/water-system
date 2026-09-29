package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

// Datos de la sección "RAZÓN DE NOTIFICACIÓN" de la carta física, capturados
// cuando se marca un aviso ya generado como entregado. Ver AvisoAdeudoService.
@Data
public class AvisoAdeudoEntregaRequestDto implements Serializable {
    // "TITULAR" | "OTRA_PERSONA" | "SE_NEGO" | "NO_ENCONTRADO"
    private String tipoEntrega;
    // Solo aplica cuando tipoEntrega = OTRA_PERSONA.
    private String nombreReceptor;
    private String parentescoReceptor;
    private String nombreNotificador;
    private String nombreTestigo1;
    private String nombreTestigo2;
    // Comentario libre para explicar qué sucedió (opcional).
    private String comentarioEntrega;
    // Solo aplica cuando tipoEntrega = ABONO -- folio del recibo que ya se
    // capturó por el abono, como soporte. Se valida contra agua_recibo
    // antes de guardarse (mismo criterio que en marcarAtendida) y, si viene,
    // también cierra automáticamente la alerta de "pendiente de atención"
    // (equivale a marcarla atendida con resultado PAGADO).
    private Integer folioReciboVinculado;
    // Opcional -- si no se manda, se usa la fecha/hora del servidor al
    // momento de registrar la entrega.
    private LocalDateTime fechaEntrega;
}
