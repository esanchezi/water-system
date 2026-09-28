package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterUserRevisionRequestDto;
import com.mx.uvas.watersystem.response.WaterUserRevisionRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterUserRevisionService {

    ResponseEntity<WaterUserRevisionRestResponse> registrar(Integer aguaUsuarioId, WaterUserRevisionRequestDto request);

    ResponseEntity<WaterUserRevisionRestResponse> historialPorUsuario(Integer aguaUsuarioId);

    ResponseEntity<WaterUserRevisionRestResponse> eliminar(Integer revisionId);
}
