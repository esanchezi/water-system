package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.WaterAgreementDto;
import com.mx.uvas.watersystem.helpers.WaterAgreementHelper;
import com.mx.uvas.watersystem.helpers.WaterHelper;
import com.mx.uvas.watersystem.mapping.WaterAgreementMapper;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterAgreementEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IWaterAgreementRepository;
import com.mx.uvas.watersystem.response.WaterAgreementRestResponse;
import com.mx.uvas.watersystem.services.IWaterAgreementService;
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
public class WaterAgreementService implements IWaterAgreementService {

    private final IWaterAgreementRepository waterAgreementRepository;
    private final WaterAgreementMapper waterAgreementMapper;
    private final WaterAgreementHelper waterAgreementHelper;
    private final WaterHelper waterHelper;

    private static final String CONVENIOS_FOUND_MESSAGE = "Convenios encontrados";
    private static final String ERROR_SEARCHING_CONVENIOS_MESSAGE = "Error al consultar convenios";
    private static final String CONVENIO_CREATED_MESSAGE = "Convenio registrado correctamente";
    private static final String ERROR_CREATING_CONVENIO_MESSAGE = "Error al registrar el convenio";

    @Override
    public ResponseEntity<WaterAgreementRestResponse> findAll() {
        return handleFindAll(waterAgreementRepository.findAllActivos());
    }

    @Override
    public ResponseEntity<WaterAgreementRestResponse> findByNoUser(Integer noUser) {
        return handleFindAll(waterAgreementRepository.findByNoUser(noUser));
    }

    @Override
    public ResponseEntity<WaterAgreementRestResponse> create(WaterAgreementDto request) {
        WaterAgreementRestResponse response = new WaterAgreementRestResponse();
        try {
            WaterUserEntity user = waterHelper.getWaterUser(request.getNoUsuario());
            CatalogOptionsEntity estatusConvenio = request.getEstatusConvenioId() != null
                    ? waterHelper.getCatalogOptionOrThrow(request.getEstatusConvenioId())
                    : null;

            WaterAgreementEntity toPersist = waterAgreementHelper.buildEntity(request, user, estatusConvenio);

            if (request.getCargos() != null) {
                request.getCargos().forEach(lineRequest ->
                        toPersist.addCargo(waterAgreementHelper.buildChargeLine(lineRequest, toPersist)));
            }

            waterAgreementRepository.save(toPersist);

            response.setData(List.of(waterAgreementMapper.entityToDto(toPersist)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, CONVENIO_CREATED_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, e.getMessage());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_CREATING_CONVENIO_MESSAGE, e);
        }
    }

    private ResponseEntity<WaterAgreementRestResponse> handleFindAll(List<WaterAgreementEntity> agreements) {
        WaterAgreementRestResponse response = new WaterAgreementRestResponse();
        try {
            var dtos = agreements.isEmpty()
                    ? Collections.<WaterAgreementDto>emptyList()
                    : agreements.stream()
                    .map(waterAgreementMapper::entityToDto)
                    .toList();

            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, CONVENIOS_FOUND_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_CONVENIOS_MESSAGE, e);
        }
    }

}
