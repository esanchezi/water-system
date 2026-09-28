package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.CajaValvulaDto;
import com.mx.uvas.watersystem.response.CajaValvulaRestResponse;
import com.mx.uvas.watersystem.response.ResumenValvulasRestResponse;
import org.springframework.http.ResponseEntity;

public interface ICajaValvulaService {
    ResponseEntity<CajaValvulaRestResponse> findAll();

    ResponseEntity<CajaValvulaRestResponse> findById(Integer cajaId);

    ResponseEntity<CajaValvulaRestResponse> create(CajaValvulaDto dto);

    ResponseEntity<CajaValvulaRestResponse> update(Integer cajaId, CajaValvulaDto dto);

    ResponseEntity<CajaValvulaRestResponse> delete(Integer cajaId);

    ResponseEntity<ResumenValvulasRestResponse> resumen();
}
