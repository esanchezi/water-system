package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Un usuario de una calle, candidato a recibir un Aviso para actualización
// del padrón de habitantes (Art. 5, 15, 15 Bis y 15 Ter) -- igual que en
// Aviso de bomba, NO hay cálculo de deuda: cualquier usuario de la calle
// aplica por igual. Ver AvisoPadronService.candidatosPorCalle().
@Data
public class AvisoPadronCandidatoDto implements Serializable {
    private Integer aguaUsuarioId;
    private Integer noUsuario;
    private String nombreCompleto;
    private Integer casaNo;
    // "2-D" / "2-I" -- casaNo + lado de la casa, cuando se conoce.
    private String casaNoTexto;
    private String calleNombre;
    private String domicilioToma;
    // Estatus del Comité (catálogo) -- para poder ocultar/mostrar a los
    // dados de baja igual que en Cartas de adeudo / Aviso de bomba.
    private Integer estatusComiteId;
    private String estatusComiteNombre;
}
