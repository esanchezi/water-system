package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.WaterUserNoticeDto;
import com.mx.uvas.watersystem.helpers.WaterHelper;
import com.mx.uvas.watersystem.helpers.WaterUserNoticeHelper;
import com.mx.uvas.watersystem.mapping.WaterUserNoticeMapper;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.model.WaterUserNoticeEntity;
import com.mx.uvas.watersystem.repositories.IWaterUserNoticeRepository;
import com.mx.uvas.watersystem.response.WaterUserNoticeRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserNoticeService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterUserNoticeService implements IWaterUserNoticeService {

    private final IWaterUserNoticeRepository waterUserNoticeRepository;
    private final WaterUserNoticeMapper waterUserNoticeMapper;
    private final WaterUserNoticeHelper waterUserNoticeHelper;
    private final WaterHelper waterHelper;

    private static final String AVISOS_FOUND_MESSAGE = "Avisos encontrados";
    private static final String AVISOS_NOT_FOUND_MESSAGE = "Avisos no encontrados";
    private static final String ERROR_SEARCHING_AVISOS_MESSAGE = "Error al consultar avisos por ID";
    private static final String AVISO_CREATED_MESSAGE = "Aviso registrado correctamente";
    private static final String ERROR_CREATING_AVISO_MESSAGE = "Error al registrar el aviso";

    @Override
    public ResponseEntity<WaterUserNoticeRestResponse> findByNoUser(Integer noUser) {
        return handleFindAll(waterUserNoticeRepository.findByNoUser(noUser));
    }

    @Override
    public ResponseEntity<WaterUserNoticeRestResponse> create(WaterUserNoticeDto request) {
        WaterUserNoticeRestResponse response = new WaterUserNoticeRestResponse();
        try {
            WaterUserEntity user = waterHelper.getWaterUser(request.getNoUsuario());
            CatalogOptionsEntity estatusAviso = request.getAvisoEstatusId() != null
                    ? waterHelper.getCatalogOptionOrThrow(request.getAvisoEstatusId())
                    : null;
            CatalogOptionsEntity tipo = request.getTipoId() != null
                    ? waterHelper.getCatalogOptionOrThrow(request.getTipoId())
                    : null;
            CatalogOptionsEntity responsable = request.getResponsableId() != null
                    ? waterHelper.getCatalogOptionOrThrow(request.getResponsableId())
                    : null;

            WaterUserNoticeEntity toPersist = waterUserNoticeHelper.buildEntity(request, user, estatusAviso, tipo, responsable);
            waterUserNoticeRepository.save(toPersist);

            response.setData(List.of(waterUserNoticeMapper.entityToDto(toPersist)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, AVISO_CREATED_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, e.getMessage());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_CREATING_AVISO_MESSAGE, e);
        }
    }

    private ResponseEntity<WaterUserNoticeRestResponse> handleFindAll(List<WaterUserNoticeEntity> waterUsersNotice) {
        WaterUserNoticeRestResponse response = new WaterUserNoticeRestResponse();
        try {
            List<WaterUserNoticeDto> waterUserDtos = waterUsersNotice.isEmpty()
                    ? Collections.emptyList()
                    : waterUsersNotice.stream()
                    .map(waterUserNoticeMapper::entityToDto)
                    .toList();

            response.setData(waterUserDtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, AVISOS_FOUND_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, AVISOS_NOT_FOUND_MESSAGE);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_AVISOS_MESSAGE, e);
        }
    }

}
