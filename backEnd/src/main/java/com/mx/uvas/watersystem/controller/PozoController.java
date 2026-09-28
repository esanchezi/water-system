package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.PozoDto;
import com.mx.uvas.watersystem.response.PozoRestResponse;
import com.mx.uvas.watersystem.services.IPozoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/pozo")
@AllArgsConstructor
public class PozoController {
    private final IPozoService pozoService;

    @GetMapping("/")
    public ResponseEntity<PozoRestResponse> getAll() {
        return pozoService.findAll();
    }

    @GetMapping("/{pozoId}")
    public ResponseEntity<PozoRestResponse> getById(@PathVariable Integer pozoId) {
        return pozoService.findById(pozoId);
    }

    @PostMapping("/")
    public ResponseEntity<PozoRestResponse> create(@RequestBody PozoDto dto) {
        return pozoService.create(dto);
    }

    @PutMapping("/{pozoId}")
    public ResponseEntity<PozoRestResponse> update(@PathVariable Integer pozoId, @RequestBody PozoDto dto) {
        return pozoService.update(pozoId, dto);
    }

    @DeleteMapping("/{pozoId}")
    public ResponseEntity<PozoRestResponse> delete(@PathVariable Integer pozoId) {
        return pozoService.delete(pozoId);
    }
}
