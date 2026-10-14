package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterReceiptDto;
import com.mx.uvas.watersystem.response.WaterReceiptRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterReceiptService {
    ResponseEntity<WaterReceiptRestResponse> findAllByEstatus();

    // Recibos activos en un rango de fechas (inclusivo). Si no hay ninguno
    // regresa 200 con lista vacía (no 404), porque aquí "sin resultados" es
    // un caso normal del filtro.
    ResponseEntity<WaterReceiptRestResponse> findByRangoFechas(java.time.LocalDate desde, java.time.LocalDate hasta);

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
