package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterAgreementDto;
import com.mx.uvas.watersystem.response.WaterAgreementRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterAgreementService {

    ResponseEntity<WaterAgreementRestResponse> findAll();

    ResponseEntity<WaterAgreementRestResponse> findByNoUser(Integer noUser);

    ResponseEntity<WaterAgreementRestResponse> create(WaterAgreementDto request);

}
