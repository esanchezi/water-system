package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Para cuando la persona revisa a ojo y quiere corregir el resultado o
// dejar una nota manual (ej. "ya se corrigió en el sistema").
@Data
public class ReciboRevisionUpdateDto implements Serializable {
    private String resultado;
    private String observaciones;
}
