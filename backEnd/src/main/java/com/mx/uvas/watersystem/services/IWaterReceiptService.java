package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterReceiptDto;
import com.mx.uvas.watersystem.response.WaterReceiptRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterReceiptService {
    ResponseEntity<WaterReceiptRestResponse> findAllByEstatus();

    ResponseEntity<WaterReceiptRestResponse> findByNoFolioOrNoUsuario(Integer noFolio);

    ResponseEntity<WaterReceiptRestResponse> findByNoUsuario(Integer noFolio);

    WaterReceiptDto create(WaterReceiptDto request);

    WaterReceiptDto createCancelled(WaterReceiptDto request);
}
