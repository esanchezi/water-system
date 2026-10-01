package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class AvisoPadronGenerarRequestDto implements Serializable {
    private List<Integer> aguaUsuarioIds;
    // "Debe presentarse el día ___ de ___ de 20__" -- se pide siempre antes
    // de generar, mismo criterio que fechaPresentacion en avisos de adeudo.
    private LocalDate fechaPresentacion;
    // Motivo por el que se solicita la actualización del padrón (ej. "se
    // detectó una persona adicional habitando el domicilio", "solicitud del
    // propio usuario"...) -- opcional, se imprime en la carta cuando se
    // captura. Pedido de Ely: quiere dejar constancia de la razón concreta
    // de cada solicitud, no solo el texto genérico del reglamento.
    private String motivoSolicitud;
}
