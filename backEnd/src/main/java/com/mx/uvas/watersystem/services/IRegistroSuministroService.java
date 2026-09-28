package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.RegistroSuministroDto;
import com.mx.uvas.watersystem.response.DiasPorPozoRestResponse;
import com.mx.uvas.watersystem.response.DiasPorTramoRestResponse;
import com.mx.uvas.watersystem.response.RegistroSuministroRestResponse;
import org.springframework.http.ResponseEntity;

public interface IRegistroSuministroService {
    ResponseEntity<RegistroSuministroRestResponse> findAll();

    ResponseEntity<RegistroSuministroRestResponse> findById(Integer registroId);

    ResponseEntity<RegistroSuministroRestResponse> create(RegistroSuministroDto dto);

    ResponseEntity<RegistroSuministroRestResponse> delete(Integer registroId);

    ResponseEntity<DiasPorPozoRestResponse> diasPorPozo();

    ResponseEntity<DiasPorTramoRestResponse> diasPorTramo();
}
