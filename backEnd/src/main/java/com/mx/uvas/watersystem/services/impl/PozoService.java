package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.PozoDto;
import com.mx.uvas.watersystem.mapping.PozoMapper;
import com.mx.uvas.watersystem.model.PozoEntity;
import com.mx.uvas.watersystem.repositories.IPozoRepository;
import com.mx.uvas.watersystem.response.PozoRestResponse;
import com.mx.uvas.watersystem.services.IPozoService;
import com.mx.uvas.watersystem.utils.BaseService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class PozoService extends BaseService<PozoEntity, PozoDto, PozoRestResponse> implements IPozoService {

    private final IPozoRepository pozoRepository;
    private final PozoMapper pozoMapper;

    private static final String FOUND_MESSAGE = "Pozos encontrados";
    private static final String ERROR_MESSAGE = "Error al consultar pozos";

    @Override
    public ResponseEntity<PozoRestResponse> findAll() {
        List<PozoEntity> entities = pozoRepository.findAllByEstatusOrderByNombreAsc(1);
        return handleFindAll(entities, pozoMapper::entityToDto, PozoRestResponse::new, FOUND_MESSAGE, ERROR_MESSAGE);
    }

    @Override
    public ResponseEntity<PozoRestResponse> findById(Integer pozoId) {
        PozoRestResponse response = new PozoRestResponse();
        try {
            Optional<PozoEntity> optional = pozoRepository.findById(pozoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Pozo no encontrado con id: " + pozoId);
            }
            response.setData(List.of(pozoMapper.entityToDto(optional.get())));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, FOUND_MESSAGE);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<PozoRestResponse> create(PozoDto dto) {
        PozoRestResponse response = new PozoRestResponse();
        try {
            PozoEntity entity = pozoMapper.dtoToEntity(dto);
            entity.setEstatus(1);
            PozoEntity saved = pozoRepository.save(entity);
            response.setData(List.of(pozoMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Pozo creado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al crear pozo", e);
        }
    }

    @Override
    public ResponseEntity<PozoRestResponse> update(Integer pozoId, PozoDto dto) {
        PozoRestResponse response = new PozoRestResponse();
        try {
            Optional<PozoEntity> optional = pozoRepository.findById(pozoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Pozo no encontrado con id: " + pozoId);
            }
            PozoEntity entity = optional.get();
            entity.setNombre(dto.getNombre());
            entity.setLat(dto.getLat());
            entity.setLng(dto.getLng());
            entity.setObservaciones(dto.getObservaciones());
            PozoEntity saved = pozoRepository.save(entity);
            response.setData(List.of(pozoMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Pozo actualizado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar pozo", e);
        }
    }

    @Override
    public ResponseEntity<PozoRestResponse> delete(Integer pozoId) {
        PozoRestResponse response = new PozoRestResponse();
        try {
            Optional<PozoEntity> optional = pozoRepository.findById(pozoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Pozo no encontrado con id: " + pozoId);
            }
            PozoEntity entity = optional.get();
            entity.setEstatus(0);
            pozoRepository.save(entity);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Pozo desactivado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al desactivar pozo", e);
        }
    }
}
