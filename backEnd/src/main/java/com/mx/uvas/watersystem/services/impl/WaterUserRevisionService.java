package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.WaterUserRevisionRequestDto;
import com.mx.uvas.watersystem.mapping.WaterUserRevisionMapper;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.model.WaterUserRevisionEntity;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRevisionRepository;
import com.mx.uvas.watersystem.response.WaterUserRevisionRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserRevisionService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

// Historial de revisiones de un usuario -- física (toma/medidor), de datos
// de padrón, o general. Es un registro de solo consulta pensado como
// evidencia (ver comentario en WaterUserRevisionEntity): se guarda fecha,
// tipo, resultado y comentario libre; las fotos de respaldo se manejan
// aparte (ver WaterUserRevisionFotoService).
@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterUserRevisionService implements IWaterUserRevisionService {

    private final IWaterUserRevisionRepository waterUserRevisionRepository;
    private final IWaterUserRepository waterUserRepository;
    private final WaterUserRevisionMapper waterUserRevisionMapper;

    @Override
    public ResponseEntity<WaterUserRevisionRestResponse> registrar(Integer aguaUsuarioId, WaterUserRevisionRequestDto request) {
        WaterUserRevisionRestResponse response = new WaterUserRevisionRestResponse();
        try {
            Optional<WaterUserEntity> userOpt = waterUserRepository.findById(aguaUsuarioId);
            if (userOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Usuario no encontrado con id: " + aguaUsuarioId);
            }
            if (request.getFechaStr() == null || request.getFechaStr().isBlank()) {
                return ResponseHandler.handleBadRequest(response, "La fecha de la revisión es obligatoria");
            }
            if (request.getTipo() == null || request.getResultado() == null) {
                return ResponseHandler.handleBadRequest(response, "El tipo y el resultado de la revisión son obligatorios");
            }

            WaterUserRevisionEntity revision = WaterUserRevisionEntity.builder()
                    .fecha(LocalDate.parse(request.getFechaStr()))
                    .tipo(request.getTipo())
                    .resultado(request.getResultado())
                    .comentario(request.getComentario())
                    .estatus(1)
                    .userIdAdd(1)
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .waterUser(userOpt.get())
                    .build();

            WaterUserRevisionEntity saved = waterUserRevisionRepository.save(revision);
            response.setData(List.of(waterUserRevisionMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Revisión registrada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al registrar la revisión", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<WaterUserRevisionRestResponse> historialPorUsuario(Integer aguaUsuarioId) {
        WaterUserRevisionRestResponse response = new WaterUserRevisionRestResponse();
        try {
            List<WaterUserRevisionEntity> revisiones =
                    waterUserRevisionRepository.findByWaterUser_AguaUsuarioIdAndEstatusOrderByFechaDesc(aguaUsuarioId, 1);
            response.setData(revisiones.stream().map(waterUserRevisionMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Revisiones encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar el historial de revisiones", e);
        }
    }

    @Override
    public ResponseEntity<WaterUserRevisionRestResponse> eliminar(Integer revisionId) {
        WaterUserRevisionRestResponse response = new WaterUserRevisionRestResponse();
        try {
            Optional<WaterUserRevisionEntity> optional = waterUserRevisionRepository.findById(revisionId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Revisión no encontrada con id: " + revisionId);
            }
            WaterUserRevisionEntity revision = optional.get();
            revision.setEstatus(0);
            waterUserRevisionRepository.save(revision);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Revisión eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al eliminar la revisión", e);
        }
    }
}
