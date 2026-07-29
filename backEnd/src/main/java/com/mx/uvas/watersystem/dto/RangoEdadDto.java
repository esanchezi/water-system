package com.mx.uvas.watersystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class RangoEdadDto implements Serializable {
    private String label;
    private Integer cantidad;
}
