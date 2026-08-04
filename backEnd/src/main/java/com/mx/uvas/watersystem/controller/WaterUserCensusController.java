package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.WaterUserCensusDto;
import com.mx.uvas.watersystem.response.WaterUserCensusResumenRestResponse;
import com.mx.uvas.watersystem.response.WaterUserCensusRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserCensusService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/waterUserCensus")
@AllArgsConstructor
public class WaterUserCensusController {

    private final IWaterUserCensusService waterUserCensusService;

    @Operation(summary = "Lista el censo activo de un usuario de agua")
    @GetMapping("/usuario/{aguaUsuarioId}")
    public ResponseEntity<WaterUserCensusRestResponse> findByAguaUsuarioId(@PathVariable Integer aguaUsuarioId) {
        return waterUserCensusService.findByAguaUsuarioId(aguaUsuarioId);
    }

    @Operation(summary = "Agrega una persona al censo de un usuario (edad opcional)")
    @PostMapping("/{aguaUsuarioId}")
    public ResponseEntity<WaterUserCensusRestResponse> create(@PathVariable Integer aguaUsuarioId,
                                                               @Valid @RequestBody WaterUserCensusDto request) {
        return waterUserCensusService.create(aguaUsuarioId, request);
    }

    @Operation(summary = "Edita edad/observaciones de un integrante del censo")
    @PutMapping("/{censoId}")
    public ResponseEntity<WaterUserCensusRestResponse> update(@PathVariable Integer censoId,
                                                               @Valid @RequestBody WaterUserCensusDto request) {
        return waterUserCensusService.update(censoId, request);
    }

    @Operation(summary = "Baja lógica de un integrante del censo (no borra el registro)")
    @PutMapping("/{censoId}/deactivate")
    public ResponseEntity<WaterUserCensusRestResponse> deactivate(@PathVariable Integer censoId) {
        return waterUserCensusService.deactivate(censoId);
    }

    @Operation(summary = "Resumen agregado del censo por rango de edad (todos los usuarios)")
    @GetMapping("/resumen")
    public ResponseEntity<WaterUserCensusResumenRestResponse> resumenPorEdad() {
        return waterUserCensusService.resumenPorEdad();
    }

}
