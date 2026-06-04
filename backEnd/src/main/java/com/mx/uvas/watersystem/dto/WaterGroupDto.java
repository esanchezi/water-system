package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WaterGroupDto implements Serializable {

    private Integer grupoId;
    private String nombre;
    private String observaciones;

    private List<WaterUserDto> listWaterUser;
}
