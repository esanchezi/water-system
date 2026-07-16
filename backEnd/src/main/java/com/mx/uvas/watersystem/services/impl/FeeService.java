package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.FeeAmountDto;
import com.mx.uvas.watersystem.dto.FeeDto;
import com.mx.uvas.watersystem.mapping.FeeMapper;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.FeeAmountEntity;
import com.mx.uvas.watersystem.model.FeeEntity;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IFeeAmountRepository;
import com.mx.uvas.watersystem.repositories.IFeeRepository;
import com.mx.uvas.watersystem.response.FeeRestResponse;
import com.mx.uvas.watersystem.services.IFeeService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class FeeService implements IFeeService {

    private final IFeeRepository feeRepository;
    private final IFeeAmountRepository feeAmountRepository;
    private final ICatalogOptionsRepository catalogOptionsRepository;
    private final FeeMapper feeMapper;

    private static final String FEE_FOUND_MESSAGE = "Cuota encontrada";
    private static final String FEE_NOT_FOUND_MESSAGE = "Cuota no encontrada";
    private static final String ERROR_SEARCHING_FEE_MESSAGE = "Error al consultar cuota por ID";

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<FeeRestResponse> searchAll() {
        FeeRestResponse response = new FeeRestResponse();
        try {
            List<FeeEntity> feeEntityList = feeRepository.findAll();
            if (feeEntityList.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, FEE_NOT_FOUND_MESSAGE);
            }
            List<FeeDto> feeDtos = mapEntitiesToDtos(feeEntityList);
            buildResponse(response, feeDtos);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, FEE_NOT_FOUND_MESSAGE);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_FEE_MESSAGE, e);
        }
    }

    @Override
    public ResponseEntity<FeeRestResponse> create(FeeDto dto) {
        FeeRestResponse response = new FeeRestResponse();
        try {
            CatalogOptionsEntity uso = findCatalogOption(dto.getUsoId());
            CatalogOptionsEntity userType = findCatalogOption(dto.getUserTypeId());

            FeeEntity entity = feeMapper.dtoToEntity(dto, uso, userType);
            FeeEntity saved = feeRepository.save(entity);

            response.setData(List.of(feeMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Cuota registrada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al crear la cuota", e);
        }
    }

    @Override
    public ResponseEntity<FeeRestResponse> update(Integer cuotaId, FeeDto dto) {
        FeeRestResponse response = new FeeRestResponse();
        try {
            Optional<FeeEntity> optional = feeRepository.findById(cuotaId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Cuota no encontrada con id: " + cuotaId);
            }
            FeeEntity existing = optional.get();
            existing.setObservaciones(dto.getObservaciones());

            if (dto.getUsoId() != null) {
                existing.setUso(findCatalogOption(dto.getUsoId()));
            }
            if (dto.getUserTypeId() != null) {
                existing.setUserType(findCatalogOption(dto.getUserTypeId()));
            }

            FeeEntity updated = feeRepository.save(existing);
            response.setData(List.of(feeMapper.entityToDto(updated)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Cuota actualizada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar la cuota", e);
        }
    }

    @Override
    public ResponseEntity<FeeRestResponse> addAmount(Integer cuotaId, FeeAmountDto dto) {
        FeeRestResponse response = new FeeRestResponse();
        try {
            Optional<FeeEntity> optional = feeRepository.findById(cuotaId);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Cuota no encontrada con id: " + cuotaId);
            }
            FeeEntity fee = optional.get();
            FeeAmountEntity amount = feeMapper.dtoToAmountEntity(dto, fee);
            fee.getFeeAmount().add(amount);
            FeeEntity saved = feeRepository.save(fee);

            response.setData(List.of(feeMapper.entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Monto de cuota agregado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al agregar el monto de la cuota", e);
        }
    }

    @Override
    public ResponseEntity<FeeRestResponse> updateAmount(Integer cuotaId, Integer cuotaMontoId, FeeAmountDto dto) {
        FeeRestResponse response = new FeeRestResponse();
        try {
            Optional<FeeAmountEntity> optional = feeAmountRepository.findById(cuotaMontoId);
            if (optional.isEmpty() || optional.get().getFee() == null
                    || !optional.get().getFee().getCuotaId().equals(cuotaId)) {
                return ResponseHandler.handleNotFoundException(response, "Monto de cuota no encontrado con id: " + cuotaMontoId);
            }
            FeeAmountEntity existing = optional.get();
            existing.setCuota(dto.getCuota());
            existing.setVigencia(dto.getVigencia());
            existing.setObservaciones(dto.getObservaciones());
            feeAmountRepository.save(existing);

            FeeEntity fee = feeRepository.findById(cuotaId).orElseThrow();
            response.setData(List.of(feeMapper.entityToDto(fee)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Monto de cuota actualizado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar el monto de la cuota", e);
        }
    }

    private CatalogOptionsEntity findCatalogOption(Integer id) {
        return id != null ? catalogOptionsRepository.findById(id).orElse(null) : null;
    }

    private List<FeeDto> mapEntitiesToDtos(List<FeeEntity> feeEntities) {
        return feeEntities.stream()
                .map(feeMapper::entityToDto)
                .toList();
    }

    private void buildResponse(FeeRestResponse response, List<FeeDto> feeDtos) {
        response.setData(feeDtos);
        response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, FEE_FOUND_MESSAGE);
    }
}
