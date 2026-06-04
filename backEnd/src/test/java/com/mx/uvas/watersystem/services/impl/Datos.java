package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.model.WaterUserEntity;

import java.util.List;

public class Datos {

    public final static WaterUserEntity user1 = WaterUserEntity.builder()
            .aguaUsuarioId(1)
            .noUsuario(1001)
            .habitaDomicilio(true)
            .tieneToma(true)
            .email("usuario1@example.com")
            .observaciones("Observaciones 1")
            .estatus(1)
            .userIdAdd(1)
            .build();

    public final static WaterUserEntity user2 = WaterUserEntity.builder()
            .aguaUsuarioId(2)
            .noUsuario(1002)
            .habitaDomicilio(true)
            .tieneToma(true)
            .email("usuario2@example.com")
            .observaciones("Observaciones 2")
            .estatus(1)
            .userIdAdd(1)
            .build();

    public final static List<WaterUserEntity> simulatedUsers = List.of(user1,user2);
}
