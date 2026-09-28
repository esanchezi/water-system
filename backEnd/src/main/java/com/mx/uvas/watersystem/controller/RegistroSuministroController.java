package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.RegistroSuministroDto;
import com.mx.uvas.watersystem.response.DiasPorPozoRestResponse;
import com.mx.uvas.watersystem.response.DiasPorTramoRestResponse;
import com.mx.uvas.watersystem.response.RegistroSuministroRestResponse;
import com.mx.uvas.watersystem.services.IRegistroSuministroService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/registroSuministro")
@AllArgsConstructor
public class RegistroSuministroController {
    private final IRegistroSuministroService registroSuministroService;

    @GetMapping("/")
    public ResponseEntity<RegistroSuministroRestResponse> getAll() {
        return registroSuministroService.findAll();
    }

    @GetMapping("/{registroId}")
    public ResponseEntity<RegistroSuministroRestResponse> getById(@PathVariable Integer registroId) {
        return registroSuministroService.findById(registroId);
    }

    @PostMapping("/")
    public ResponseEntity<RegistroSuministroRestResponse> create(@RequestBody RegistroSuministroDto dto) {
        return registroSuministroService.create(dto);
    }

    @DeleteMapping("/{registroId}")
    public ResponseEntity<RegistroSuministroRestResponse> delete(@PathVariable Integer registroId) {
        return registroSuministroService.delete(registroId);
    }

    @GetMapping("/resumen/porPozo")
    public ResponseEntity<DiasPorPozoRestResponse> diasPorPozo() {
        return registroSuministroService.diasPorPozo();
    }

    @GetMapping("/resumen/porTramo")
    public ResponseEntity<DiasPorTramoRestResponse> diasPorTramo() {
        return registroSuministroService.diasPorTramo();
    }
}
