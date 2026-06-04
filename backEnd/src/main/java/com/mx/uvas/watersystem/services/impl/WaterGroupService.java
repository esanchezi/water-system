package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.WaterGroupDto;
import com.mx.uvas.watersystem.mapping.WaterGroupMapper;
import com.mx.uvas.watersystem.model.WaterGroupEntity;
import com.mx.uvas.watersystem.repositories.IWaterGroupRepository;
import com.mx.uvas.watersystem.response.WaterGroupRestResponse;
import com.mx.uvas.watersystem.services.IWaterGroupService;
import com.mx.uvas.watersystem.utils.BaseService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterGroupService extends BaseService<WaterGroupEntity, WaterGroupDto, WaterGroupRestResponse>
        implements IWaterGroupService {

    private final IWaterGroupRepository waterGroupRepository;
    private final WaterGroupMapper waterGroupMapper;

    private static final String USUARIOS_FOUND_MESSAGE = "Usuarios encontrados";
    private static final String ERROR_SEARCHING_USUARIOS_MESSAGE = "Error al consultar lista de Usuarios";

    @Override
    public ResponseEntity<WaterGroupRestResponse> findAll() {
        List<WaterGroupEntity> entities = waterGroupRepository.findAllByOrderByNombreAsc();

        return handleFindAll(
                entities,
                waterGroupMapper::entityToDto,
                WaterGroupRestResponse::new,
                USUARIOS_FOUND_MESSAGE,
                ERROR_SEARCHING_USUARIOS_MESSAGE
        );
    }

    @Override
    public ResponseEntity<WaterGroupRestResponse> createWaterGroup(WaterGroupDto dto) {
        WaterGroupRestResponse response = new WaterGroupRestResponse();
        try {
            WaterGroupEntity entity = waterGroupMapper.dtoToEntity(dto);
            WaterGroupEntity savedEntity = waterGroupRepository.save(entity);
            WaterGroupDto savedDto = waterGroupMapper.entityToDto(savedEntity);
            response.setData(List.of(savedDto));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Casa de agua creada correctamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al crear casa de agua", e);
        }
    }
}
