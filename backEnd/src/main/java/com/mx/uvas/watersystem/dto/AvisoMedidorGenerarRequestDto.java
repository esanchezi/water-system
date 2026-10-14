package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

// A diferencia de Aviso de Bomba/Padrón (que se generan en lote, para
// varios usuarios a la vez con el mismo dato de fecha), este aviso se
// genera de a uno -- cada instalación de medidor tiene su propio número de
// serie y lectura inicial, que no tendría sentido repetir para varios
// usuarios en una sola llamada.
@Data
public class AvisoMedidorGenerarRequestDto implements Serializable {
    private Integer aguaUsuarioId;
    private LocalDate fechaInstalacion;
    private String noSerieMedidor;
    private String lecturaInicialMedidor;
}
