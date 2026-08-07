package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.PreregistroUsuarioDto;
import com.mx.uvas.watersystem.model.PreregistroUsuarioEntity;
import com.mx.uvas.watersystem.model.WaterGroupEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IPreregistroUsuarioRepository;
import com.mx.uvas.watersystem.repositories.IWaterGroupRepository;
import com.mx.uvas.watersystem.repositories.IWaterHouseRepository;
import com.mx.uvas.watersystem.response.PreregistroUsuarioRestResponse;
import com.mx.uvas.watersystem.services.IPreregistroUsuarioService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.CurrentUserService;
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
    private final ICatalogOptionsRepository catalogOptionsRepository;
    private final IWaterGroupRepository waterGroupRepository;
    private final CurrentUserService currentUserService;

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
                    .esNegocio(dto.getEsNegocio())
                    .giroNegocio(dto.getGiroNegocioId() != null
                            ? catalogOptionsRepository.findById(dto.getGiroNegocioId()).orElse(null)
                            : null)
                    .motivoNoUsuario(dto.getMotivoNoUsuarioId() != null
                            ? catalogOptionsRepository.findById(dto.getMotivoNoUsuarioId()).orElse(null)
                            : null)
                    .deudaAportaciones(dto.getDeudaAportaciones())
                    .deudaMultasRecargos(dto.getDeudaMultasRecargos())
                    .deudaObservaciones(dto.getDeudaObservaciones())
                    .waterGroup(dto.getGrupoId() != null
                            ? waterGroupRepository.findById(dto.getGrupoId()).orElse(null)
                            : null)
                    .userIdAdd(currentUserService.getCurrentUserId())
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
            entity.setEsNegocio(dto.getEsNegocio());
            entity.setGiroNegocio(dto.getGiroNegocioId() != null
                    ? catalogOptionsRepository.findById(dto.getGiroNegocioId()).orElse(null)
                    : null);
            entity.setMotivoNoUsuario(dto.getMotivoNoUsuarioId() != null
                    ? catalogOptionsRepository.findById(dto.getMotivoNoUsuarioId()).orElse(null)
                    : null);
            entity.setDeudaAportaciones(dto.getDeudaAportaciones());
            entity.setDeudaMultasRecargos(dto.getDeudaMultasRecargos());
            entity.setDeudaObservaciones(dto.getDeudaObservaciones());
            entity.setWaterGroup(dto.getGrupoId() != null
                    ? waterGroupRepository.findById(dto.getGrupoId()).orElse(null)
                    : null);
            entity.setUserIdUpdate(currentUserService.getCurrentUserId());
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
            entity.setUserIdUpdate(currentUserService.getCurrentUserId());
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
            entity.setUserIdUpdate(currentUserService.getCurrentUserId());
            entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            preregistroRepository.save(entity);

            return findByCasaId(entity.getWaterHouse().getCasaId());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al descartar el preregistro", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PreregistroUsuarioRestResponse> findConDeuda() {
        PreregistroUsuarioRestResponse response = new PreregistroUsuarioRestResponse();
        try {
            List<PreregistroUsuarioDto> dtos = preregistroRepository.findAllConDeudaNoConvertidos()
                    .stream()
                    .map(this::entityToDto)
                    .toList();

            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Preregistros con deuda encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar preregistros con deuda", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PreregistroUsuarioRestResponse> findAllConDetalle() {
        PreregistroUsuarioRestResponse response = new PreregistroUsuarioRestResponse();
        try {
            List<PreregistroUsuarioDto> dtos = preregistroRepository.findAllConDetalle()
                    .stream()
                    .map(this::entityToDto)
                    .toList();

            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Preregistros encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar el listado de preregistro", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PreregistroUsuarioRestResponse> findByGrupoId(Integer grupoId) {
        PreregistroUsuarioRestResponse response = new PreregistroUsuarioRestResponse();
        try {
            List<PreregistroUsuarioDto> dtos = preregistroRepository.findByWaterGroup_GrupoId(grupoId)
                    .stream()
                    .map(this::entityToDto)
                    .toList();

            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Preregistros del grupo encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar preregistros del grupo", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<PreregistroUsuarioRestResponse> asignarGrupo(Integer preregistroId, Integer grupoId) {
        PreregistroUsuarioRestResponse response = new PreregistroUsuarioRestResponse();
        try {
            Optional<PreregistroUsuarioEntity> optional = preregistroRepository.findById(preregistroId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, NOT_FOUND_MESSAGE);
            }

            PreregistroUsuarioEntity entity = optional.get();
            WaterGroupEntity grupo = grupoId != null ? waterGroupRepository.findById(grupoId).orElse(null) : null;
            entity.setWaterGroup(grupo);
            entity.setUserIdUpdate(currentUserService.getCurrentUserId());
            entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            preregistroRepository.save(entity);

            response.setData(List.of(entityToDto(entity)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Grupo actualizado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al asignar el grupo", e);
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
        dto.setEsNegocio(entity.getEsNegocio());
        if (entity.getGiroNegocio() != null) {
            dto.setGiroNegocioId(entity.getGiroNegocio().getCatalogoOpcionesId());
            dto.setGiroNegocioNombre(entity.getGiroNegocio().getNombre());
        }
        if (entity.getMotivoNoUsuario() != null) {
            dto.setMotivoNoUsuarioId(entity.getMotivoNoUsuario().getCatalogoOpcionesId());
            dto.setMotivoNoUsuarioNombre(entity.getMotivoNoUsuario().getNombre());
        }
        dto.setDeudaAportaciones(entity.getDeudaAportaciones());
        dto.setDeudaMultasRecargos(entity.getDeudaMultasRecargos());
        dto.setDeudaObservaciones(entity.getDeudaObservaciones());
        if (entity.getWaterHouse() != null) {
            dto.setCasaNo(entity.getWaterHouse().getCasaNo());
            if (entity.getWaterHouse().getCatCalle() != null) {
                dto.setCalleNombre(entity.getWaterHouse().getCatCalle().getNombre());
            }
        }
        if (entity.getWaterGroup() != null) {
            dto.setGrupoId(entity.getWaterGroup().getGrupoId());
            dto.setGrupoNombre(entity.getWaterGroup().getNombre());
        }
        return dto;
    }
}
