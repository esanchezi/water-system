package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.ValorGeneralDto;
import com.mx.uvas.watersystem.response.ValorGeneralRestResponse;
import com.mx.uvas.watersystem.services.IValorGeneralService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Administración de la tabla de valores generales (multa, corte/reconexión,
// aviso, interés moratorio por día, multa de válvulas) -- ver
// ValorGeneralEntity / ValorGeneralClave.
@RestController
@RequestMapping(path = "/api/v1/valorGeneral")
@AllArgsConstructor
public class ValorGeneralController {

    private final IValorGeneralService valorGeneralService;

    @GetMapping("/")
    public ResponseEntity<ValorGeneralRestResponse> findAll() {
        return valorGeneralService.findAll();
    }

    @PostMapping("/")
    public ResponseEntity<ValorGeneralRestResponse> create(@RequestBody ValorGeneralDto dto) {
        return valorGeneralService.create(dto);
    }

    @PutMapping("/{valorGeneralId}")
    public ResponseEntity<ValorGeneralRestResponse> update(@PathVariable Integer valorGeneralId, @RequestBody ValorGeneralDto dto) {
        return valorGeneralService.update(valorGeneralId, dto);
    }

    @PutMapping("/{valorGeneralId}/baja")
    public ResponseEntity<ValorGeneralRestResponse> deactivate(@PathVariable Integer valorGeneralId) {
        return valorGeneralService.deactivate(valorGeneralId);
    }

}
