package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class AvisoResponsablePagoGenerarRequestDto implements Serializable {
    private Integer casaId;
    private String motivoSolicitud;
    private LocalDate fechaSolicitud;
    private String observacionesComite;
    private List<AvisoResponsablePagoPersonaInputDto> personas;
}
