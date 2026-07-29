package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class PreregistroUsuarioDto implements Serializable {

    private Integer preregistroId;
    private Integer casaId;
    private String nombre;
    private String telefono;
    private String observaciones;
    private LocalDate fechaRegistro;
    private Integer estatus;
    private String motivoPendiente;
    private Integer aguaUsuarioIdConvertido;
}
