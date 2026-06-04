package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.CatalogDto;
import com.mx.uvas.watersystem.dto.CatalogOptionsDto;
import com.mx.uvas.watersystem.mapping.CatalogMapper;
import com.mx.uvas.watersystem.model.CatalogEntity;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.ICatalogRepository;
import com.mx.uvas.watersystem.response.CatalogRestResponse;
import com.mx.uvas.watersystem.services.ICatalogService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class CatalogService implements ICatalogService {

    private final ICatalogRepository catalogRepository;
    private final ICatalogOptionsRepository catalogOptionsRepository;
    private final CatalogMapper catalogMapper;

    private static final String FOUND_ALL      = "Catálogos encontrados";
    private static final String FOUND          = "Catálogo encontrado";
    private static final String NOT_FOUND      = "Catálogo no encontrado";
    private static final String CREATED        = "Catálogo creado correctamente";
    private static final String UPDATED        = "Catálogo actualizado correctamente";
    private static final String DEACTIVATED    = "Catálogo desactivado";
    private static final String OPT_CREATED    = "Opción agregada correctamente";
    private static final String OPT_UPDATED    = "Opción actualizada correctamente";
    private static final String OPT_DEACTIVATED = "Opción desactivada";
    private static final String OPT_NOT_FOUND  = "Opción de catálogo no encontrada";

    // ── GET ALL (solo activos) ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CatalogRestResponse> findAll() {
        CatalogRestResponse response = new CatalogRestResponse();
        try {
            List<CatalogEntity> entities = catalogRepository.findByEstatus(1);
            List<CatalogDto> dtos = entities.stream()
                    .map(catalogMapper::entityToDto)
                    .toList();
            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, FOUND_ALL);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar catálogos", e);
        }
    }

    // ── GET BY ID ──────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CatalogRestResponse> searchById(Integer id) {
        CatalogRestResponse response = new CatalogRestResponse();
        try {
            catalogRepository.findById(id).ifPresentOrElse(
                    entity -> {
                        response.setData(Collections.singletonList(catalogMapper.entityToDto(entity)));
                        response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, FOUND);
                    },
                    () -> ResponseHandler.handleNotFoundException(response, NOT_FOUND + " [ID: " + id + "]")
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar catálogo", e);
        }
    }

    // ── CREATE ─────────────────────────────────────────────────────────────
    @Override
    public ResponseEntity<CatalogRestResponse> create(CatalogDto dto) {
        CatalogRestResponse response = new CatalogRestResponse();
        try {
            CatalogEntity entity = catalogMapper.dtoToEntity(dto);
            CatalogEntity saved = catalogRepository.save(entity);
            response.setData(List.of(catalogMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, CREATED);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al crear catálogo", e);
        }
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────
    @Override
    public ResponseEntity<CatalogRestResponse> update(Integer id, CatalogDto dto) {
        CatalogRestResponse response = new CatalogRestResponse();
        try {
            Optional<CatalogEntity> optional = catalogRepository.findById(id);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, NOT_FOUND + " [ID: " + id + "]");
            }
            CatalogEntity entity = optional.get();
            entity.setNombre(dto.getNombre());
            entity.setDescripcion(dto.getDescripcion());
            entity.setUserIdUpdate(1); // TODO: Keycloak
            entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            CatalogEntity updated = catalogRepository.save(entity);
            response.setData(List.of(catalogMapper.entityToDto(updated)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, UPDATED);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar catálogo", e);
        }
    }

    // ── DEACTIVATE (soft-delete estatus=0) ────────────────────────────────
    @Override
    public ResponseEntity<CatalogRestResponse> deactivate(Integer id) {
        CatalogRestResponse response = new CatalogRestResponse();
        try {
            Optional<CatalogEntity> optional = catalogRepository.findById(id);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, NOT_FOUND + " [ID: " + id + "]");
            }
            CatalogEntity entity = optional.get();
            entity.setEstatus(0);
            entity.setUserIdUpdate(1); // TODO: Keycloak
            entity.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            catalogRepository.save(entity);
            response.setData(Collections.emptyList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, DEACTIVATED);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al desactivar catálogo", e);
        }
    }

    // ── ADD OPTION ─────────────────────────────────────────────────────────
    @Override
    public ResponseEntity<CatalogRestResponse> addOption(Integer catalogoId, CatalogOptionsDto dto) {
        CatalogRestResponse response = new CatalogRestResponse();
        try {
            Optional<CatalogEntity> optional = catalogRepository.findById(catalogoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, NOT_FOUND + " [ID: " + catalogoId + "]");
            }
            CatalogOptionsEntity option = catalogMapper.optionDtoToEntity(dto, optional.get());
            catalogOptionsRepository.save(option);
            CatalogEntity reloaded = catalogRepository.findById(catalogoId).get();
            response.setData(List.of(catalogMapper.entityToDto(reloaded)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, OPT_CREATED);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al agregar opción", e);
        }
    }

    // ── UPDATE OPTION ──────────────────────────────────────────────────────
    @Override
    public ResponseEntity<CatalogRestResponse> updateOption(Integer catalogoId, Integer opcionId, CatalogOptionsDto dto) {
        CatalogRestResponse response = new CatalogRestResponse();
        try {
            Optional<CatalogOptionsEntity> optional = catalogOptionsRepository.findById(opcionId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, OPT_NOT_FOUND + " [ID: " + opcionId + "]");
            }
            CatalogOptionsEntity option = optional.get();
            option.setNombre(dto.getNombre());
            option.setDescripcion(dto.getDescripcion());
            option.setUserIdUpdate(1); // TODO: Keycloak
            option.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            catalogOptionsRepository.save(option);
            CatalogEntity reloaded = catalogRepository.findById(catalogoId).get();
            response.setData(List.of(catalogMapper.entityToDto(reloaded)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, OPT_UPDATED);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar opción", e);
        }
    }

    // ── DEACTIVATE OPTION ──────────────────────────────────────────────────
    @Override
    public ResponseEntity<CatalogRestResponse> deactivateOption(Integer catalogoId, Integer opcionId) {
        CatalogRestResponse response = new CatalogRestResponse();
        try {
            Optional<CatalogOptionsEntity> optional = catalogOptionsRepository.findById(opcionId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, OPT_NOT_FOUND + " [ID: " + opcionId + "]");
            }
            CatalogOptionsEntity option = optional.get();
            option.setEstatus(0);
            option.setUserIdUpdate(1); // TODO: Keycloak
            option.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            catalogOptionsRepository.save(option);
            CatalogEntity reloaded = catalogRepository.findById(catalogoId).get();
            response.setData(List.of(catalogMapper.entityToDto(reloaded)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, OPT_DEACTIVATED);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al desactivar opción", e);
        }
    }
}
