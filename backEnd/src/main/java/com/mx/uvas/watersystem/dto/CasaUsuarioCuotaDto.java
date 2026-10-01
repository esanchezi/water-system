package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Usuario ya dado de alta y ligado a una casa, con su cuota vigente -- para
// el selector "agregar usuario existente" del formulario de Aviso
// Responsables de Pago (ver AvisoResponsablePagoService.usuariosDeLaCasa()).
// cuotaVigente puede venir null si el usuario no tiene cuota asignada o no
// hay monto capturado para el año actual.
@Data
public class CasaUsuarioCuotaDto implements Serializable {
    private Integer aguaUsuarioId;
    private Integer noUsuario;
    private String nombreCompleto;
    private Double cuotaVigente;
}
