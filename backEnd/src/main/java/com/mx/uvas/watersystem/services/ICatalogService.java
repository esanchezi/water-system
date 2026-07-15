package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.CatalogDto;
import com.mx.uvas.watersystem.dto.CatalogOptionsDto;
import com.mx.uvas.watersystem.response.CatalogRestResponse;
import org.springframework.http.ResponseEntity;

public interface ICatalogService {

    // ── Catálogos maestros ──────────────────────────────────────────────────
    ResponseEntity<CatalogRestResponse> findAll();
    ResponseEntity<CatalogRestResponse> searchById(Integer id);
    ResponseEntity<CatalogRestResponse> searchByClave(String clave);
    ResponseEntity<CatalogRestResponse> create(CatalogDto dto);
    ResponseEntity<CatalogRestResponse> update(Integer id, CatalogDto dto);
    ResponseEntity<CatalogRestResponse> deactivate(Integer id);

    // ── Opciones de un catálogo ─────────────────────────────────────────────
    ResponseEntity<CatalogRestResponse> addOption(Integer catalogoId, CatalogOptionsDto dto);
    ResponseEntity<CatalogRestResponse> updateOption(Integer catalogoId, Integer opcionId, CatalogOptionsDto dto);
    ResponseEntity<CatalogRestResponse> deactivateOption(Integer catalogoId, Integer opcionId);
}
