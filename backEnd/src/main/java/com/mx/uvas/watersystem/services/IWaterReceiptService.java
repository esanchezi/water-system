package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterReceiptDto;
import com.mx.uvas.watersystem.response.WaterReceiptRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterReceiptService {
    ResponseEntity<WaterReceiptRestResponse> findAllByEstatus();

    ResponseEntity<WaterReceiptRestResponse> findByNoFolioOrNoUsuario(Integer noFolio);

    ResponseEntity<WaterReceiptRestResponse> findByNoUsuario(Integer noFolio);

    // Búsqueda exacta por folio (no ambigua, a diferencia de
    // findByNoFolioOrNoUsuario que también matchea por número de usuario) --
    // pensada para detectar en el frontend si un folio ya fue capturado
    // antes de guardar un recibo nuevo.
    ResponseEntity<WaterReceiptRestResponse> findByNoFolioExacto(Integer noFolio);

    WaterReceiptDto create(WaterReceiptDto request);

    ResponseEntity<WaterReceiptRestResponse> update(Integer id, WaterReceiptDto request);

    WaterReceiptDto createCancelled(WaterReceiptDto request);
}
