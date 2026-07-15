package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterUserChargeDto;
import com.mx.uvas.watersystem.dto.WaterUserChargePaymentDto;
import com.mx.uvas.watersystem.response.WaterUserChargeRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterUserChargeService {

    ResponseEntity<WaterUserChargeRestResponse> findByNoUser(Integer noUser);

    ResponseEntity<WaterUserChargeRestResponse> findPendientesByNoUser(Integer noUser);

    ResponseEntity<WaterUserChargeRestResponse> create(WaterUserChargeDto request);

    // Registra un abono (parcial o total) de un cargo, ligado a un recibo.
    ResponseEntity<WaterUserChargeRestResponse> addPayment(Integer aguaUsuarioCargoId, WaterUserChargePaymentDto request);

}
