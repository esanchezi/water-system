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
    // Opcional -- si no se manda, se usa la fecha/hora del servidor al
    // momento de registrar la entrega.
    private LocalDateTime fechaEntrega;
}
