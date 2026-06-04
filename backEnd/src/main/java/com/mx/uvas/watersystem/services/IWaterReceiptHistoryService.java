package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterReceiptHistoryDto;
import com.mx.uvas.watersystem.response.WaterReceiptHistoryRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterReceiptHistoryService {
    ResponseEntity<WaterReceiptHistoryRestResponse> findAllByEstatus();

    ResponseEntity<WaterReceiptHistoryRestResponse> findByNoFolioOrNombre(String noFolio);

    WaterReceiptHistoryDto updateReceiptHistory(WaterReceiptHistoryDto request);

}
