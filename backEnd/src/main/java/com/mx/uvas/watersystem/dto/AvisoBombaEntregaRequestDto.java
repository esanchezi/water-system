package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

// Datos de la sección "RAZÓN DE NOTIFICACIÓN" de la carta física, capturados
// cuando se marca un aviso de bomba ya generado como entregado -- mismo
// shape que AvisoAdeudoEntregaRequestDto.
@Data
public class AvisoBombaEntregaRequestDto implements Serializable {
    private String tipoEntrega;
    private String nombreReceptor;
    private String parentescoReceptor;
    private String nombreNotificador;
    private String nombreTestigo1;
    private String nombreTestigo2;
    private LocalDateTime fechaEntrega;
}
