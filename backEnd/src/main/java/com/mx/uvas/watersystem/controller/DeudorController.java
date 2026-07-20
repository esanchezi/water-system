package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.response.DeudorRestResponse;
import com.mx.uvas.watersystem.services.IDeudorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/deudores")
@AllArgsConstructor
public class DeudorController {

    private final IDeudorService deudorService;

    @Operation(summary = "Reporte de deudores por casa: cuota anual pendiente + cargos/multas pendientes")
    @GetMapping("/")
    public ResponseEntity<DeudorRestResponse> getDeudores(@RequestParam(required = false) Integer anio) {
        return deudorService.getDeudores(anio);
    }
}
