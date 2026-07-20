package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.WaterUserAnnualPaymentDto;
import com.mx.uvas.watersystem.response.WaterUserAnnualPaymentRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserAnnualPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/waterUserAnnualPayment")
@AllArgsConstructor
public class WaterUserAnnualPaymentController {

    private final IWaterUserAnnualPaymentService waterUserAnnualPaymentService;

    @Operation(summary = "Lista los años pagados/dados de baja de un usuario")
    @GetMapping("/{noUser}")
    public ResponseEntity<WaterUserAnnualPaymentRestResponse> findByNoUser(@PathVariable Integer noUser) {
        return waterUserAnnualPaymentService.findByNoUser(noUser);
    }

    @Operation(summary = "Marca un año como pagado para un usuario")
    @PostMapping("/{aguaUsuarioId}")
    public ResponseEntity<WaterUserAnnualPaymentRestResponse> create(@PathVariable Integer aguaUsuarioId,
                                                                      @Valid @RequestBody WaterUserAnnualPaymentDto request) {
        return waterUserAnnualPaymentService.create(aguaUsuarioId, request);
    }

    @Operation(summary = "Baja lógica: quita la marca de año pagado")
    @PutMapping("/{pagoAnualId}/deactivate")
    public ResponseEntity<WaterUserAnnualPaymentRestResponse> deactivate(@PathVariable Integer pagoAnualId) {
        return waterUserAnnualPaymentService.deactivate(pagoAnualId);
    }

}
