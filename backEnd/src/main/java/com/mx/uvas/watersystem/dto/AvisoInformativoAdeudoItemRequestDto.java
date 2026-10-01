package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Un usuario elegido a mano para recibir el Aviso Informativo de Adeudo --
// la selección es manual (ver clarificación de Ely: "selección manual...
// como flujo principal"), pero el adeudo/multa/último pago se calculan
// igual que en Cartas de adeudo, con AdeudoLuzService.calcularParaUsuarios()
// (ver AvisoInformativoAdeudoService.generar()) -- Ely pidió explícitamente
// que el cálculo sea "lo que ya se tiene", no capturado a mano.
@Data
public class AvisoInformativoAdeudoItemRequestDto implements Serializable {
    private Integer aguaUsuarioId;
    // Nota libre del Comité (opcional) -- lo único que sigue siendo manual.
    private String observacion;
}
