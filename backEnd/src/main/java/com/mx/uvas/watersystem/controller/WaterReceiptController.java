package com.mx.uvas.watersystem.controller;


import com.mx.uvas.watersystem.dto.WaterReceiptDto;
import com.mx.uvas.watersystem.response.WaterReceiptRestResponse;
import com.mx.uvas.watersystem.services.IWaterReceiptService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/waterReceipt" )
@AllArgsConstructor
public class WaterReceiptController {

    private final IWaterReceiptService waterReceiptService;

    @GetMapping("/")
    public ResponseEntity<WaterReceiptRestResponse> findAllByEstatus() {
        return waterReceiptService.findAllByEstatus();
    }

    @GetMapping("/findByNoFolioOrNoUsuario/{no}")
    public ResponseEntity<WaterReceiptRestResponse> findByNoFolioOrNoUsuario(@PathVariable Integer no) {
        return waterReceiptService.findByNoFolioOrNoUsuario(no);
    }

    @GetMapping("/findByNoUsuario/{noUser}")
    public ResponseEntity<WaterReceiptRestResponse> findByNoUsuario(@PathVariable Integer noUser) {
        return waterReceiptService.findByNoUsuario(noUser);
    }

    @PostMapping("/")
    public ResponseEntity<WaterReceiptDto> post (@RequestBody WaterReceiptDto request){
        return ResponseEntity.ok(waterReceiptService.create(request));
    }

    @PostMapping("/cancelled")
    public ResponseEntity<WaterReceiptDto> receiptCancelled (@RequestBody WaterReceiptDto request){
        return ResponseEntity.ok(waterReceiptService.createCancelled(request));
    }

    /*@Operation(summary = "Crea persona, direccion y usuario")
    @PostMapping(path = "/setUser")
    public ResponseEntity<WaterUserDto> setUser (@Valid @RequestBody WaterUserDto request){
        return ResponseEntity.ok(waterUserService.create(request));
    }*/
}
