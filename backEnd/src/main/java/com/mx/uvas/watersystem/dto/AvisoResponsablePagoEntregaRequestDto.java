package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

// Datos de la sección "RAZÓN DE NOTIFICACIÓN" de la carta física, capturados
// cuando se marca esta carta ya generada como entregada -- mismo shape que
// AvisoAdeudoEntregaRequestDto/AvisoBombaEntregaRequestDto/AvisoPadronEntregaRequestDto.
@Data
public class AvisoResponsablePagoEntregaRequestDto implements Serializable {
    private String tipoEntrega;
    private String nombreReceptor;
    private String parentescoReceptor;
    private String nombreNotificador;
    private String nombreTestigo1;
    private String nombreTestigo2;
    private String comentarioEntrega;
    private LocalDateTime fechaEntrega;
}
