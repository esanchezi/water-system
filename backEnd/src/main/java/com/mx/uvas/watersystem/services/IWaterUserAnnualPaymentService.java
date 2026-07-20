package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterUserAnnualPaymentDto;
import com.mx.uvas.watersystem.response.WaterUserAnnualPaymentRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterUserAnnualPaymentService {

    ResponseEntity<WaterUserAnnualPaymentRestResponse> findByNoUser(Integer noUser);

    ResponseEntity<WaterUserAnnualPaymentRestResponse> create(Integer aguaUsuarioId, WaterUserAnnualPaymentDto dto);

    // Baja lógica: quita el año de "pagado" (estatus = 0), no borra el registro.
    ResponseEntity<WaterUserAnnualPaymentRestResponse> deactivate(Integer pagoAnualId);
}
