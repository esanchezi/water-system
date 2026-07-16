package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.WaterHouseDto;
import com.mx.uvas.watersystem.mapping.WaterHouseMapper;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IWaterHouseRepository;
import com.mx.uvas.watersystem.response.WaterHouseRestResponse;
import com.mx.uvas.watersystem.services.IWaterHouseService;
import com.mx.uvas.watersystem.utils.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterHouseService extends BaseService<WaterHouseEntity, WaterHouseDto, WaterHouseRestResponse>
        implements IWaterHouseService {

    private final IWaterHouseRepository waterHouseRepository;
    private final ICatalogOptionsRepository catalogOptionsRepository;
    private final WaterHouseMapper waterHouseMapper;

    private static final String USUARIOS_FOUND_MESSAGE = "Usuarios encontrados";
    private static final String ERROR_SEARCHING_USUARIOS_MESSAGE = "Error al consultar lista de Usuarios";

    @Override
    public ResponseEntity<WaterHouseRestResponse> findAll() {
        List<WaterHouseEntity> entities = waterHouseRepository.findAllByOrderByCatCalle_NombreAscLadoAscCasaNoAsc();

        return handleFindAll(
                entities,
                waterHouseMapper::entityToDto,
                WaterHouseRestResponse::new,
                USUARIOS_FOUND_MESSAGE,
                ERROR_SEARCHING_USUARIOS_MESSAGE
        );
    }

    @Override
    public ResponseEntity<WaterHouseRestResponse> createWaterHouse(WaterHouseDto dto) {
        WaterHouseRestResponse response = new WaterHouseRestResponse();
        try {
            CatalogOptionsEntity calle = dto.getCalleId() != null
                    ? catalogOptionsRepository.findById(dto.getCalleId()).orElse(null)
                    : null;

            WaterHouseEntity entity = waterHouseMapper.dtoToEntity(dto, calle);
            WaterHouseEntity savedEntity = waterHouseRepository.save(entity);
            WaterHouseDto savedDto = waterHouseMapper.entityToDto(savedEntity);
            response.setData(List.of(savedDto));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Casa de agua creada correctamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al crear casa de agua", e);
        }
    }

    public ResponseEntity<WaterHouseRestResponse> updateWaterHouse(Integer casaId, WaterHouseDto dto) {
        WaterHouseRestResponse response = new WaterHouseRestResponse();
        try {
            Optional<WaterHouseEntity> optional = waterHouseRepository.findById(casaId);

            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Casa de agua no encontrada con id: " + casaId);
            }

            WaterHouseEntity existing = optional.get();

            // Actualiza campos usando el DTO
            existing.setCasaNo(dto.getCasaNo());
            existing.setNombre(dto.getNombre());
            if (dto.getLat() != null) {
                existing.setLat(dto.getLat());
            }
            if (dto.getLng() != null) {
                existing.setLng(dto.getLng());
            }
            existing.setObservaciones(dto.getObservaciones());
            existing.setLado(dto.getLado());

            CatalogOptionsEntity calle = dto.getCalleId() != null
                    ? catalogOptionsRepository.findById(dto.getCalleId()).orElse(null)
                    : null;
            existing.setCatCalle(calle);

            existing.setDateUpdate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

            WaterHouseEntity updated = waterHouseRepository.save(existing);
            WaterHouseDto updatedDto = waterHouseMapper.entityToDto(updated);

            response.setData(List.of(updatedDto));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Casa de agua actualizada correctamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar casa de agua", e);
        }
    }
}
