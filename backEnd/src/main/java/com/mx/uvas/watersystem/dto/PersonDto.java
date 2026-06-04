package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PersonDto implements Serializable {
    private Integer personaId;
    private String nombre;
    private String nombre2;
    private String app;
    private String apm;
    private String curp;
}
