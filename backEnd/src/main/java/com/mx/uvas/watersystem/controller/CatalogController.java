package com.mx.uvas.watersystem.controller;


import com.mx.uvas.watersystem.response.CatalogRestResponse;
import com.mx.uvas.watersystem.services.ICatalogService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/catalog" )
@AllArgsConstructor
public class CatalogController {

    private final ICatalogService catalogService;

    @GetMapping("/{id}")
    public ResponseEntity<CatalogRestResponse> searchCatalogoById(@PathVariable Integer id) {
        return catalogService.searchById(id);
    }
}
