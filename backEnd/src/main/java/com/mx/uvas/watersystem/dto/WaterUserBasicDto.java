package com.mx.uvas.watersystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class WaterUserBasicDto implements Serializable {
        private Integer noUsuario;
        private Integer usuarioId;
        private Integer personaId;
        private Integer direccionId;
        private Integer casaId;
        private String nombre;
        private String nombre2;
        private String app;
        private String apm;
        private String alias;
        private String direccion;
        private String casa;
        private Integer calleId;
        private Integer casaNo;
        private String calleNombre;
        private String calleTexto;
        private String numeroTexto;
        // Sección (catálogo) capturada en la dirección del usuario -- sirve
        // para desambiguar calles con el mismo nombre en secciones distintas
        // cuando el usuario aún no tiene casa asignada (solo texto libre).
        private Integer seccionTextoId;
    }