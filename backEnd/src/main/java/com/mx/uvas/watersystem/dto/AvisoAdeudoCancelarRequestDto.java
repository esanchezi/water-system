package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Motivo opcional de la cancelación -- pedido explícito de Ely: sobre todo
// para dejar constancia de POR QUÉ se cancela cuando es porque la carta no
// fue entregada/recibida (se negaron a recibir, no se encontró a nadie,
// etc.), en vez de perder ese contexto una vez que el aviso queda oculto
// del historial por default. Body opcional -- si no se manda nada, se
// cancela igual que antes, sin motivo.
@Data
public class AvisoAdeudoCancelarRequestDto implements Serializable {
    private String comentarioCancela;
}
