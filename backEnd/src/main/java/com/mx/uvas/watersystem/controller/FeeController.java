package com.mx.uvas.watersystem.controller;


import com.mx.uvas.watersystem.dto.FeeAmountDto;
import com.mx.uvas.watersystem.dto.FeeDto;
import com.mx.uvas.watersystem.response.FeeRestResponse;
import com.mx.uvas.watersystem.services.IFeeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/fee" )
@AllArgsConstructor
public class FeeController {

    private final IFeeService feeService;

    @GetMapping("/")
    public ResponseEntity<FeeRestResponse> searchAll() {
        return feeService.searchAll();
    }

    @PostMapping("/")
    public ResponseEntity<FeeRestResponse> create(@RequestBody FeeDto dto) {
        return feeService.create(dto);
    }

    @PutMapping("/{cuotaId}")
    public ResponseEntity<FeeRestResponse> update(@PathVariable Integer cuotaId, @RequestBody FeeDto dto) {
        return feeService.update(cuotaId, dto);
    }

    @PostMapping("/{cuotaId}/amounts")
    public ResponseEntity<FeeRestResponse> addAmount(@PathVariable Integer cuotaId, @RequestBody FeeAmountDto dto) {
        return feeService.addAmount(cuotaId, dto);
    }

    @PutMapping("/{cuotaId}/amounts/{cuotaMontoId}")
    public ResponseEntity<FeeRestResponse> updateAmount(
            @PathVariable Integer cuotaId,
            @PathVariable Integer cuotaMontoId,
            @RequestBody FeeAmountDto dto
    ) {
        return feeService.updateAmount(cuotaId, cuotaMontoId, dto);
    }

}
