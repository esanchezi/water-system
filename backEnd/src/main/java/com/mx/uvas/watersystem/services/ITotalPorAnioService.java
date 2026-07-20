package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.response.TotalPorAnioRestResponse;
import org.springframework.http.ResponseEntity;

public interface ITotalPorAnioService {

    ResponseEntity<TotalPorAnioRestResponse> getTotalesPorAnio();
}
