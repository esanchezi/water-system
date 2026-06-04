package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WaterUserNoticeDto implements Serializable {
    private Integer aguaUsuarioAvisoId;
    private String aviso;
    private String comentario;
    private CatalogOptionsDto estatusAviso;
}
