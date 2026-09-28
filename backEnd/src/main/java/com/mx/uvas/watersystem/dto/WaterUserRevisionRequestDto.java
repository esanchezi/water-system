package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Lo que manda el frontend al registrar una revisión -- fecha como String
// "yyyy-MM-dd" (mismo patrón timezone-safe ya usado en el resto de la app,
// se convierte a LocalDate sin hora en el service, evita el corrimiento de
// un día por zona horaria).
@Data
public class WaterUserRevisionRequestDto implements Serializable {
    private String fechaStr;
    private String tipo;
    private String resultado;
    private String comentario;
}
