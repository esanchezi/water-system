package com.mx.uvas.watersystem.controller;


import com.mx.uvas.watersystem.dto.WaterHouseDto;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.response.WaterHouseRestResponse;
import com.mx.uvas.watersystem.services.IWaterHouseService;
import com.mx.uvas.watersystem.utils.RestResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/waterHouse" )
@AllArgsConstructor
public class WaterHouseController {
    private final IWaterHouseService waterHouseService;
    @GetMapping("/")
    public ResponseEntity<WaterHouseRestResponse> getListWaterHouse() {
        return waterHouseService.findAll();
    }

    @GetMapping("/{casaId}")
    public ResponseEntity<WaterHouseRestResponse> getById(@PathVariable Integer casaId) {
        return waterHouseService.findById(casaId);
    }

    @PostMapping(path = "/")
    public ResponseEntity<WaterHouseRestResponse> create(@RequestBody WaterHouseDto dto) {
        return waterHouseService.createWaterHouse(dto);
    }

    @PutMapping("/{casaId}")
    public ResponseEntity<WaterHouseRestResponse> update(
            @PathVariable Integer casaId,
            @RequestBody WaterHouseDto request
    ) {
        return waterHouseService.updateWaterHouse(casaId, request);
    }
}
