package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Lo que manda el frontend por cada renglón de la tabla al generar la carta
// -- ver AvisoResponsablePagoGenerarRequestDto.
@Data
public class AvisoResponsablePagoPersonaInputDto implements Serializable {
    private String nombreCompleto;
    private String parentesco;
    private Integer familiaCuota;

    // Opcional: si se eligió del autocomplete de usuarios ya dados de alta
    // en esta casa (ver AvisoResponsablePagoService.usuariosDeLaCasa()).
    private Integer aguaUsuarioId;
}
