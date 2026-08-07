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

    // Negocio que no va a tener su propio usuario de agua (ver comentario
    // en PreregistroUsuarioEntity), pero que sí debe contarse en el censo
    // de negocios.
    private Boolean esNegocio;
    private Integer giroNegocioId;
    private String giroNegocioNombre;

    // Catálogo MOTIVO_NOUSUARIO.
    private Integer motivoNoUsuarioId;
    private String motivoNoUsuarioNombre;

    // Deuda aproximada (estimación manual, no viene de cargos reales --
    // esta persona todavía no es usuario formal).
    private java.math.BigDecimal deudaAportaciones;
    private java.math.BigDecimal deudaMultasRecargos;
    private String deudaObservaciones;

    // Info de ubicación de la casa -- solo para listados que muestran
    // preregistros de varias casas a la vez (ej. deudores); no se necesita
    // en la pantalla de una sola casa donde ya se sabe cuál es.
    private String calleNombre;
    private Integer casaNo;

    // Grupo al que probablemente se va a unir esta persona cuando se
    // convierta en usuario formal (opcional).
    private Integer grupoId;
    private String grupoNombre;
}
