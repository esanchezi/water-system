package com.mx.uvas.watersystem.helpers;

import com.mx.uvas.watersystem.dto.WaterUserNoticeDto;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.model.WaterUserNoticeEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Transactional
@Component
@AllArgsConstructor
public class WaterUserNoticeHelper {

    public WaterUserNoticeEntity buildEntity(WaterUserNoticeDto request, WaterUserEntity user, CatalogOptionsEntity estatusAviso,
                                              CatalogOptionsEntity tipo, CatalogOptionsEntity responsable) {
        return WaterUserNoticeEntity.builder()
                .waterUser(user)
                .aviso(request.getAviso())
                .comentario(request.getComentario())
                .estatusAviso(estatusAviso)
                .tipo(tipo)
                .responsable(responsable)
                .estatus(1)
                .userIdAdd(1) // TODO: Keycloak
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}
