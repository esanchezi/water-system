package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.CatalogDto;
import com.mx.uvas.watersystem.dto.CatalogOptionsDto;
import com.mx.uvas.watersystem.response.CatalogRestResponse;
import com.mx.uvas.watersystem.services.ICatalogService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/catalog")
@AllArgsConstructor
public class CatalogController {

    private final ICatalogService catalogService;

    // ── Catálogos maestros ──────────────────────────────────────────────────

    @GetMapping("/")
    public ResponseEntity<CatalogRestResponse> getAll() {
        return catalogService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogRestResponse> getById(@PathVariable Integer id) {
        return catalogService.searchById(id);
    }

    @GetMapping("/clave/{clave}")
    public ResponseEntity<CatalogRestResponse> getByClave(@PathVariable String clave) {
        return catalogService.searchByClave(clave);
    }

    @PostMapping("/")
    public ResponseEntity<CatalogRestResponse> create(@RequestBody CatalogDto dto) {
        return catalogService.create(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogRestResponse> update(
            @PathVariable Integer id,
            @RequestBody CatalogDto dto) {
        return catalogService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CatalogRestResponse> deactivate(@PathVariable Integer id) {
        return catalogService.deactivate(id);
    }

    // ── Opciones de catálogo ────────────────────────────────────────────────

    @PostMapping("/{catalogoId}/options")
    public ResponseEntity<CatalogRestResponse> addOption(
            @PathVariable Integer catalogoId,
            @RequestBody CatalogOptionsDto dto) {
        return catalogService.addOption(catalogoId, dto);
    }

    @PutMapping("/{catalogoId}/options/{opcionId}")
    public ResponseEntity<CatalogRestResponse> updateOption(
            @PathVariable Integer catalogoId,
            @PathVariable Integer opcionId,
            @RequestBody CatalogOptionsDto dto) {
        return catalogService.updateOption(catalogoId, opcionId, dto);
    }

    @DeleteMapping("/{catalogoId}/options/{opcionId}")
    public ResponseEntity<CatalogRestResponse> deactivateOption(
            @PathVariable Integer catalogoId,
            @PathVariable Integer opcionId) {
        return catalogService.deactivateOption(catalogoId, opcionId);
    }
}
