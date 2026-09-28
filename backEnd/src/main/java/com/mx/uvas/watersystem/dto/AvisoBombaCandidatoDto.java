package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Un usuario de una calle, candidato a recibir un Aviso por uso indebido de
// bomba (Art. 23) -- a diferencia de los candidatos a carta de adeudo, aquí
// NO hay cálculo de deuda: cualquier usuario de la calle aplica por igual
// (el aviso se emite por un reporte de uso de bomba, no por adeudo). Ver
// AvisoBombaService.candidatosPorCalle().
@Data
public class AvisoBombaCandidatoDto implements Serializable {
    private Integer aguaUsuarioId;
    private Integer noUsuario;
    private String nombreCompleto;
    private Integer casaNo;
    // "2-D" / "2-I" -- casaNo + lado de la casa, cuando se conoce.
    private String casaNoTexto;
    private String calleNombre;
    private String domicilioToma;
    // Estatus del Comité (catálogo) -- para poder ocultar/mostrar a los
    // dados de baja igual que en Cartas de adeudo.
    private Integer estatusComiteId;
    private String estatusComiteNombre;
}
