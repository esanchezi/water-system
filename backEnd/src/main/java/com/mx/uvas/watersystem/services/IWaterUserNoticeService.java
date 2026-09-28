package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterUserNoticeDto;
import com.mx.uvas.watersystem.response.WaterUserNoticeRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterUserNoticeService {

    ResponseEntity<WaterUserNoticeRestResponse> findByNoUser(Integer noUser);

    ResponseEntity<WaterUserNoticeRestResponse> create(WaterUserNoticeDto request);

    // Para marcar un aviso/nota como Atendido/Cerrado (o cualquier otro
    // estatus del catálogo ESTATUS_AVISO) -- ver alerta de avisos
    // pendientes al consultar la ficha del usuario.
    ResponseEntity<WaterUserNoticeRestResponse> updateEstatus(Integer aguaUsuarioAvisoId, Integer avisoEstatusId);

}
