package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ResumenAnualDto implements Serializable {
    private List<ResumenAnualFilaDto> filas;
}
