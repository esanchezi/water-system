package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.response.CatalogRestResponse;
import org.springframework.http.ResponseEntity;

public interface ICatalogService {
    ResponseEntity<CatalogRestResponse> searchById(Integer id);
}
