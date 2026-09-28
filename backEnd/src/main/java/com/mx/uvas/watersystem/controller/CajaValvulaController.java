package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.CajaValvulaDto;
import com.mx.uvas.watersystem.response.CajaValvulaRestResponse;
import com.mx.uvas.watersystem.response.ResumenValvulasRestResponse;
import com.mx.uvas.watersystem.services.ICajaValvulaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/cajaValvula")
@AllArgsConstructor
public class CajaValvulaController {
    private final ICajaValvulaService cajaValvulaService;

    @GetMapping("/")
    public ResponseEntity<CajaValvulaRestResponse> getAll() {
        return cajaValvulaService.findAll();
    }

    @GetMapping("/{cajaId}")
    public ResponseEntity<CajaValvulaRestResponse> getById(@PathVariable Integer cajaId) {
        return cajaValvulaService.findById(cajaId);
    }

    @PostMapping("/")
    public ResponseEntity<CajaValvulaRestResponse> create(@RequestBody CajaValvulaDto dto) {
        return cajaValvulaService.create(dto);
    }

    @PutMapping("/{cajaId}")
    public ResponseEntity<CajaValvulaRestResponse> update(@PathVariable Integer cajaId, @RequestBody CajaValvulaDto dto) {
        return cajaValvulaService.update(cajaId, dto);
    }

    @DeleteMapping("/{cajaId}")
    public ResponseEntity<CajaValvulaRestResponse> delete(@PathVariable Integer cajaId) {
        return cajaValvulaService.delete(cajaId);
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenValvulasRestResponse> resumen() {
        return cajaValvulaService.resumen();
    }
}
