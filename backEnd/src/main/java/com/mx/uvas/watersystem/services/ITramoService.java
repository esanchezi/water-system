package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.TramoDto;
import com.mx.uvas.watersystem.response.TramoRestResponse;
import org.springframework.http.ResponseEntity;

public interface ITramoService {
    ResponseEntity<TramoRestResponse> findAll();

    ResponseEntity<TramoRestResponse> findById(Integer tramoId);

    ResponseEntity<TramoRestResponse> create(TramoDto dto);

    ResponseEntity<TramoRestResponse> update(Integer tramoId, TramoDto dto);

    ResponseEntity<TramoRestResponse> delete(Integer tramoId);
}
