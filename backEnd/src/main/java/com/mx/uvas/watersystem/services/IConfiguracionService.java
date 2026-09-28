package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.ConfiguracionDto;
import com.mx.uvas.watersystem.response.ConfiguracionRestResponse;
import org.springframework.http.ResponseEntity;

public interface IConfiguracionService {

    ResponseEntity<ConfiguracionRestResponse> findAll();

    ResponseEntity<ConfiguracionRestResponse> updateByClave(String clave, ConfiguracionDto dto);
}
