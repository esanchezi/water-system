package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.WaterUserChargeDto;
import com.mx.uvas.watersystem.dto.WaterUserChargePaymentDto;
import com.mx.uvas.watersystem.helpers.WaterHelper;
import com.mx.uvas.watersystem.helpers.WaterUserChargeHelper;
import com.mx.uvas.watersystem.mapping.WaterUserChargeMapper;
import com.mx.uvas.watersystem.model.WaterUserChargeEntity;
import com.mx.uvas.watersystem.model.WaterUserChargePaymentEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IWaterUserChargeRepository;
import com.mx.uvas.watersystem.response.WaterUserChargeRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserChargeService;
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
import java.util.Optional;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class WaterUserChargeService implements IWaterUserChargeService {

    private final IWaterUserChargeRepository waterUserChargeRepository;
    private final WaterUserChargeMapper waterUserChargeMapper;
    private final WaterUserChargeHelper waterUserChargeHelper;
    private final WaterHelper waterHelper;

    private static final String CARGOS_FOUND_MESSAGE = "Cargos encontrados";
    private static final String CARGOS_NOT_FOUND_MESSAGE = "Cargos no encontrados";
    private static final String ERROR_SEARCHING_CARGOS_MESSAGE = "Error al consultar cargos por usuario";
    private static final String CARGO_CREATED_MESSAGE = "Cargo registrado correctamente";
    private static final String ERROR_CREATING_CARGO_MESSAGE = "Error al registrar el cargo";
    private static final String CARGO_NOT_FOUND_MESSAGE = "Cargo no encontrado";
    private static final String PAGO_CREATED_MESSAGE = "Abono registrado correctamente";
    private static final String ERROR_CREATING_PAGO_MESSAGE = "Error al registrar el abono";

    @Override
    public ResponseEntity<WaterUserChargeRestResponse> findByNoUser(Integer noUser) {
        return handleFindAll(waterUserChargeRepository.findByNoUser(noUser));
    }

    @Override
    public ResponseEntity<WaterUserChargeRestResponse> findPendientesByNoUser(Integer noUser) {
        List<WaterUserChargeEntity> pendientes = waterUserChargeRepository.findByNoUser(noUser).stream()
                .filter(cargo -> !cargo.isPagado())
                .toList();
        return handleFindAll(pendientes);
    }

    @Override
    public ResponseEntity<WaterUserChargeRestResponse> create(WaterUserChargeDto request) {
        WaterUserChargeRestResponse response = new WaterUserChargeRestResponse();
        try {
            WaterUserEntity user = waterHelper.getWaterUser(request.getNoUsuario());
            var concepto = waterHelper.getCatalogOptionOrThrow(request.getConceptoId());

            WaterUserChargeEntity toPersist = waterUserChargeHelper.buildEntity(request, user, concepto);
            waterUserChargeRepository.save(toPersist);

            response.setData(List.of(waterUserChargeMapper.entityToDto(toPersist)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, CARGO_CREATED_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, e.getMessage());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_CREATING_CARGO_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<WaterUserChargeRestResponse> addPayment(Integer aguaUsuarioCargoId, WaterUserChargePaymentDto request) {
        WaterUserChargeRestResponse response = new WaterUserChargeRestResponse();
        try {
            Optional<WaterUserChargeEntity> optional = waterUserChargeRepository.findById(aguaUsuarioCargoId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, CARGO_NOT_FOUND_MESSAGE + " [ID: " + aguaUsuarioCargoId + "]");
            }

            WaterUserChargeEntity cargo = optional.get();
            WaterUserChargePaymentEntity pago = waterUserChargeHelper.buildPayment(request, cargo);
            cargo.addPago(pago);
            waterUserChargeRepository.save(cargo);

            response.setData(List.of(waterUserChargeMapper.entityToDto(cargo)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, PAGO_CREATED_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, e.getMessage());
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_CREATING_PAGO_MESSAGE, e);
        }
    }

    private ResponseEntity<WaterUserChargeRestResponse> handleFindAll(List<WaterUserChargeEntity> charges) {
        WaterUserChargeRestResponse response = new WaterUserChargeRestResponse();
        try {
            var dtos = charges.isEmpty()
                    ? Collections.<WaterUserChargeDto>emptyList()
                    : charges.stream()
                    .map(waterUserChargeMapper::entityToDto)
                    .toList();

            response.setData(dtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, CARGOS_FOUND_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, CARGOS_NOT_FOUND_MESSAGE);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_CARGOS_MESSAGE, e);
        }
    }

}
