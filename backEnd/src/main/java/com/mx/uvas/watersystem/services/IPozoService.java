package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.PozoDto;
import com.mx.uvas.watersystem.response.PozoRestResponse;
import org.springframework.http.ResponseEntity;

public interface IPozoService {
    ResponseEntity<PozoRestResponse> findAll();

    ResponseEntity<PozoRestResponse> findById(Integer pozoId);

    ResponseEntity<PozoRestResponse> create(PozoDto dto);

    ResponseEntity<PozoRestResponse> update(Integer pozoId, PozoDto dto);

    ResponseEntity<PozoRestResponse> delete(Integer pozoId);
}
