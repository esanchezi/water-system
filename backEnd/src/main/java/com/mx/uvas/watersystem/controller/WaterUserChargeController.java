package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.WaterUserChargeDto;
import com.mx.uvas.watersystem.dto.WaterUserChargePaymentDto;
import com.mx.uvas.watersystem.response.WaterUserChargeRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserChargeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/waterUserCharge")
@AllArgsConstructor
public class WaterUserChargeController {

    private final IWaterUserChargeService waterUserChargeService;

    @GetMapping("/{noUser}")
    public ResponseEntity<WaterUserChargeRestResponse> searchByUser(@PathVariable Integer noUser) {
        return waterUserChargeService.findByNoUser(noUser);
    }

    @GetMapping("/{noUser}/pendientes")
    public ResponseEntity<WaterUserChargeRestResponse> searchPendientesByUser(@PathVariable Integer noUser) {
        return waterUserChargeService.findPendientesByNoUser(noUser);
    }

    @Operation(summary = "Lista todos los cargos (multas, recargos, etc.) de todos los usuarios de un grupo")
    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<WaterUserChargeRestResponse> searchByGrupo(@PathVariable Integer grupoId) {
        return waterUserChargeService.findByGrupoId(grupoId);
    }

    @PostMapping("/")
    public ResponseEntity<WaterUserChargeRestResponse> create(@RequestBody WaterUserChargeDto request) {
        return waterUserChargeService.create(request);
    }

    // Registra un abono (parcial o total) de un cargo; se puede llamar varias
    // veces con distintos folios hasta saldar el cargo.
    @PostMapping("/{aguaUsuarioCargoId}/pagos")
    public ResponseEntity<WaterUserChargeRestResponse> addPayment(
            @PathVariable Integer aguaUsuarioCargoId,
            @RequestBody WaterUserChargePaymentDto request) {
        return waterUserChargeService.addPayment(aguaUsuarioCargoId, request);
    }

}
