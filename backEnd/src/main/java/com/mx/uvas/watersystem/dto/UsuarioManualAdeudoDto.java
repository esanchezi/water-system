package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Usuario YA registrado en el sistema al que se le quiere generar carta con
// un monto de adeudo capturado A MANO, en vez del calculado automáticamente
// por AdeudoLuzService -- para casos donde el cálculo automático no aplica
// o no lo detecta como candidato (ej. Ely reportó un usuario cuya cuenta se
// "juntó" con la de su hija, así que el saldo de Luz queda en $0 aunque en
// realidad sí debe). A diferencia de UsuarioNoRegistradoDto, aquí SÍ hay un
// aguaUsuarioId real -- la carta se liga a su cuenta y queda en su
// historial, igual que una generada por el flujo normal.
@Data
public class UsuarioManualAdeudoDto implements Serializable {
    private Integer aguaUsuarioId;
    private Double montoAdeudo;
    // Motivo/explicación de por qué se capturó el monto a mano en vez de
    // dejarlo calcular solo -- se imprime en la carta como nota del Comité,
    // para que quede constancia de la razón (ej. "cuenta unida con la de su
    // hija, usuario 512; el saldo de luz se calculó a mano con base en el
    // expediente físico").
    private String observacion;
}
