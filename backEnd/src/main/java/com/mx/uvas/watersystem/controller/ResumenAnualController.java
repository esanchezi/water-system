package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.response.ResumenAnualRestResponse;
import com.mx.uvas.watersystem.services.IResumenAnualService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/resumenAnual")
@AllArgsConstructor
public class ResumenAnualController {

    private final IResumenAnualService resumenAnualService;

    @Operation(summary = "Resumen de caja por año: inicial/recibos caja/efectivo/egresos/saldo, con desglose mensual y por categoría")
    @GetMapping("/")
    public ResponseEntity<ResumenAnualRestResponse> getResumenAnual() {
        return resumenAnualService.getResumenAnual();
    }
}
