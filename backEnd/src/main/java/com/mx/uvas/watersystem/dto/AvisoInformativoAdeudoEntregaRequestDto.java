package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

// Datos de la sección "RAZÓN DE NOTIFICACIÓN" de la carta física, capturados
// cuando se marca un Aviso Informativo de Adeudo ya generado como entregado
// -- mismo shape que AvisoAdeudoEntregaRequestDto, sin el campo de
// folioReciboVinculado (este módulo no maneja cobro/abono, es informativo).
@Data
public class AvisoInformativoAdeudoEntregaRequestDto implements Serializable {
    private String tipoEntrega;
    private String nombreReceptor;
    private String parentescoReceptor;
    private String nombreNotificador;
    private String nombreTestigo1;
    private String nombreTestigo2;
    private String comentarioEntrega;
    private LocalDateTime fechaEntrega;
}
