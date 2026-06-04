package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.response.FeeRestResponse;
import org.springframework.http.ResponseEntity;

public interface IFeeService {
    ResponseEntity<FeeRestResponse> searchAll();
}
