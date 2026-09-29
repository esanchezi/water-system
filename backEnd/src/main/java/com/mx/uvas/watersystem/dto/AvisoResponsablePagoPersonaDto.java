package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AvisoResponsablePagoPersonaDto implements Serializable {
    private Integer responsablePagoPersonaId;
    private Integer orden;
    private String nombreCompleto;
    private String parentesco;
    private Integer familiaCuota;

    // Presentes solo si esta persona está ligada a un usuario ya dado de
    // alta (ver AvisoResponsablePagoPersonaEntity.waterUser).
    private Integer aguaUsuarioId;
    private Integer noUsuario;
    private Double cuotaVigenteSnapshot;
}
