package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.FeeAmountDto;
import com.mx.uvas.watersystem.dto.FeeDto;
import com.mx.uvas.watersystem.response.FeeRestResponse;
import org.springframework.http.ResponseEntity;

public interface IFeeService {
    ResponseEntity<FeeRestResponse> searchAll();

    ResponseEntity<FeeRestResponse> create(FeeDto dto);

    ResponseEntity<FeeRestResponse> update(Integer cuotaId, FeeDto dto);

    ResponseEntity<FeeRestResponse> addAmount(Integer cuotaId, FeeAmountDto dto);

    ResponseEntity<FeeRestResponse> updateAmount(Integer cuotaId, Integer cuotaMontoId, FeeAmountDto dto);

    ResponseEntity<FeeRestResponse> deactivate(Integer cuotaId);

    ResponseEntity<FeeRestResponse> deactivateAmount(Integer cuotaId, Integer cuotaMontoId);
}
