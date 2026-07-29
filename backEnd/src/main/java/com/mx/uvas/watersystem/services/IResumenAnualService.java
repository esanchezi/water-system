package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.response.ResumenAnualRestResponse;
import org.springframework.http.ResponseEntity;

public interface IResumenAnualService {

    ResponseEntity<ResumenAnualRestResponse> getResumenAnual();
}
