package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.FeeDto;
import com.mx.uvas.watersystem.mapping.FeeMapper;
import com.mx.uvas.watersystem.model.FeeEntity;
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

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class FeeService implements IFeeService {

    private final IFeeRepository feeRepository;
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
