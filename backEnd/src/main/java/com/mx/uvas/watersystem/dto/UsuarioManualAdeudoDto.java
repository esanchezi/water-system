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
    // expediente físico"). No aplica cuando calcularAutomatico es true.
    private String observacion;
    // true (default en el frontend) = este usuario, aunque se agregó a mano
    // porque a Ely le resultaba complicado ubicarlo en la lista de
    // candidatos filtrada por calle, SÍ se calcula automático con
    // AdeudoLuzService igual que cualquier candidato (desglose por año,
    // multa, cargos automáticos de Aviso/Mantenimiento) -- montoAdeudo se
    // ignora en ese caso. false = comportamiento original: el monto viene
    // 100% capturado a mano (para el caso real de "cálculo automático no
    // aplica", ej. cuenta juntada con la de otro familiar).
    private Boolean calcularAutomatico;
}
