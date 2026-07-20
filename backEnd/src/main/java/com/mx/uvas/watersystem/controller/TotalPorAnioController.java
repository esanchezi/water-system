package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.response.TotalPorAnioRestResponse;
import com.mx.uvas.watersystem.services.ITotalPorAnioService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/totalPorAnio")
@AllArgsConstructor
public class TotalPorAnioController {

    private final ITotalPorAnioService totalPorAnioService;

    @Operation(summary = "Totales por año y concepto de cobro (solo recibos válidos, folio > 4099)")
    @GetMapping("/")
    public ResponseEntity<TotalPorAnioRestResponse> getTotalesPorAnio() {
        return totalPorAnioService.getTotalesPorAnio();
    }
}
