package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.PreregistroUsuarioDto;
import com.mx.uvas.watersystem.model.PreregistroUsuarioEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.repositories.IPreregistroUsuarioRepository;
import com.mx.uvas.watersystem.repositories.IWaterHouseRepository;
import com.mx.uvas.watersystem.response.PreregistroUsuarioRestResponse;
import com.mx.uvas.watersystem.services.IPreregistroUsuarioService;
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

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class PreregistroUsuarioService implements IPreregistroUsuarioService {

    private final IPreregistroUsuarioRepository preregistroRepository;
    private final IWaterHouseRepository waterHouseRepository;

    private static final String NOT_FOUND_MESSAGE = "Preregistro no encontrado";
    private static final String CASA_NOT_FOUND_MESSAGE = "Casa no encontrada";

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PreregistroUsuarioRestResponse> findByCasaId(Integer casaId) {
        PreregistroUsuarioRestResponse response = new PreregistroUsuarioRestResponse();
        try {
            List<PreregistroUsuarioDto> dtos = preregistroRepository
                    .findByWaterHouse_CasaIdOrderByFechaRegistroDesc(casaId)
                    .stream()
                    .map(this::entityToDto)
                    .toList();

            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Preregistros encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar preregistros", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<PreregistroUsuarioRestResponse> create(Integer casaId, PreregistroUsuarioDto dto) {
        PreregistroUsuarioRestResponse response = new PreregistroUsuarioRestResponse();
        try {
            Optional<WaterHouseEntity> casaOpt = waterHouseRepository.findById(casaId);
            if (casaOpt.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, CASA_NOT_FOUND_MESSAGE);
            }

            PreregistroUsuarioEntity entity = PreregistroUsuarioEntity.builder()
                    .waterHouse(casaOpt.get())
                    .nombre(dto.getNombre())
                    .telefono(dto.getTelefono())
                    .observaciones(dto.getObservaciones())
                    .fechaRegistro(dto.getFechaRegistro() != null ? dto.getFechaRegistro() : LocalDate.now())
                    .estatus(PreregistroUsuarioEntity.ESTATUS_PENDIENTE)
                    .motivoPendiente(dto.getMotivoPendiente())
                    .userIdAdd(1)
                    .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .build();

            preregistroRepository.save(entity);

            return findByCasaId(casaId);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al agregar el preregistro", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<PreregistroUsuarioRestResponse> update(Integer preregistroId, PreregistroUsuarioDto dto) {
        PreregistroUsuarioRestResponse response = new PreregistroUsuarioRestResponse();
        try {
            Optional<PreregistroUsuarioEntity> optional = preregistroRepository.findById(preregistroId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, NOT_FOUND_MESSAGE);
            }

            PreregistroUsuarioEntity entity = optional.get();
            entity.setNombre(dto.getNombre());
            entity.setTelefono(dto.getTelefono());
            entity.setObservaciones(dto.getObservaciones());
            entity.setMotivoPendiente(dto.getMotivoPendiente());
            entity.setUserIdUpdate(1);
            entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            preregistroRepository.save(entity);

            return findByCasaId(entity.getWaterHouse().getCasaId());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar el preregistro", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<PreregistroUsuarioRestResponse> marcarConvertido(Integer preregistroId, Integer aguaUsuarioId) {
        PreregistroUsuarioRestResponse response = new PreregistroUsuarioRestResponse();
        try {
            Optional<PreregistroUsuarioEntity> optional = preregistroRepository.findById(preregistroId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, NOT_FOUND_MESSAGE);
            }

            PreregistroUsuarioEntity entity = optional.get();
            entity.setEstatus(PreregistroUsuarioEntity.ESTATUS_CONVERTIDO);
            entity.setAguaUsuarioIdConvertido(aguaUsuarioId);
            entity.setUserIdUpdate(1);
            entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            preregistroRepository.save(entity);

            return findByCasaId(entity.getWaterHouse().getCasaId());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al marcar como convertido", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<PreregistroUsuarioRestResponse> marcarDescartado(Integer preregistroId, String motivo) {
        PreregistroUsuarioRestResponse response = new PreregistroUsuarioRestResponse();
        try {
            Optional<PreregistroUsuarioEntity> optional = preregistroRepository.findById(preregistroId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, NOT_FOUND_MESSAGE);
            }

            PreregistroUsuarioEntity entity = optional.get();
            entity.setEstatus(PreregistroUsuarioEntity.ESTATUS_DESCARTADO);
            if (motivo != null && !motivo.isBlank()) {
                entity.setMotivoPendiente(motivo);
            }
            entity.setUserIdUpdate(1);
            entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            preregistroRepository.save(entity);

            return findByCasaId(entity.getWaterHouse().getCasaId());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al descartar el preregistro", e);
        }
    }

    private PreregistroUsuarioDto entityToDto(PreregistroUsuarioEntity entity) {
        PreregistroUsuarioDto dto = new PreregistroUsuarioDto();
        dto.setPreregistroId(entity.getPreregistroId());
        dto.setCasaId(entity.getWaterHouse() != null ? entity.getWaterHouse().getCasaId() : null);
        dto.setNombre(entity.getNombre());
        dto.setTelefono(entity.getTelefono());
        dto.setObservaciones(entity.getObservaciones());
        dto.setFechaRegistro(entity.getFechaRegistro());
        dto.setEstatus(entity.getEstatus());
        dto.setMotivoPendiente(entity.getMotivoPendiente());
        dto.setAguaUsuarioIdConvertido(entity.getAguaUsuarioIdConvertido());
        return dto;
    }
}
