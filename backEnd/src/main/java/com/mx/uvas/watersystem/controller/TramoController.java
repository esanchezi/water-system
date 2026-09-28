package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.TramoDto;
import com.mx.uvas.watersystem.response.TramoRestResponse;
import com.mx.uvas.watersystem.services.ITramoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/tramo")
@AllArgsConstructor
public class TramoController {
    private final ITramoService tramoService;

    @GetMapping("/")
    public ResponseEntity<TramoRestResponse> getAll() {
        return tramoService.findAll();
    }

    @GetMapping("/{tramoId}")
    public ResponseEntity<TramoRestResponse> getById(@PathVariable Integer tramoId) {
        return tramoService.findById(tramoId);
    }

    @PostMapping("/")
    public ResponseEntity<TramoRestResponse> create(@RequestBody TramoDto dto) {
        return tramoService.create(dto);
    }

    @PutMapping("/{tramoId}")
    public ResponseEntity<TramoRestResponse> update(@PathVariable Integer tramoId, @RequestBody TramoDto dto) {
        return tramoService.update(tramoId, dto);
    }

    @DeleteMapping("/{tramoId}")
    public ResponseEntity<TramoRestResponse> delete(@PathVariable Integer tramoId) {
        return tramoService.delete(tramoId);
    }
}
