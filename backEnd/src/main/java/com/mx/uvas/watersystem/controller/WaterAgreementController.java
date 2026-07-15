package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.WaterAgreementDto;
import com.mx.uvas.watersystem.response.WaterAgreementRestResponse;
import com.mx.uvas.watersystem.services.IWaterAgreementService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/waterAgreement")
@AllArgsConstructor
public class WaterAgreementController {

    private final IWaterAgreementService waterAgreementService;

    @GetMapping("/")
    public ResponseEntity<WaterAgreementRestResponse> getAll() {
        return waterAgreementService.findAll();
    }

    @GetMapping("/{noUser}")
    public ResponseEntity<WaterAgreementRestResponse> searchByUser(@PathVariable Integer noUser) {
        return waterAgreementService.findByNoUser(noUser);
    }

    @PostMapping("/")
    public ResponseEntity<WaterAgreementRestResponse> create(@RequestBody WaterAgreementDto request) {
        return waterAgreementService.create(request);
    }

}
