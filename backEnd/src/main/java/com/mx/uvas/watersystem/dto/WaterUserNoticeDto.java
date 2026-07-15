package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WaterUserNoticeDto implements Serializable {
    private Integer aguaUsuarioAvisoId;
    private Integer noUsuario;
    private String aviso;
    private String comentario;
    private Integer avisoEstatusId;
    private CatalogOptionsDto estatusAviso;
    private Integer tipoId;
    private CatalogOptionsDto tipo;
    private Integer responsableId;
    private CatalogOptionsDto responsable;
}
