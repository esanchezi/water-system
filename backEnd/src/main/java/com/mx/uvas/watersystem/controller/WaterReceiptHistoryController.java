package com.mx.uvas.watersystem.controller;


import com.mx.uvas.watersystem.dto.WaterReceiptHistoryDto;
import com.mx.uvas.watersystem.response.WaterReceiptHistoryRestResponse;
import com.mx.uvas.watersystem.services.IWaterReceiptHistoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/waterReceiptHistory" )
@AllArgsConstructor
public class WaterReceiptHistoryController {

    private final IWaterReceiptHistoryService waterReceiptService;

    @GetMapping("/")
    public ResponseEntity<WaterReceiptHistoryRestResponse> findAllByEstatus() {
        return waterReceiptService.findAllByEstatus();
    }

    @GetMapping("/findByNoFolioOrNombre/{no}")
    public ResponseEntity<WaterReceiptHistoryRestResponse> findByNoFolioOrNombre(@PathVariable String no) {
        return waterReceiptService.findByNoFolioOrNombre(no);
    }

    @PostMapping("/updateReceiptHistory")
    public ResponseEntity<WaterReceiptHistoryDto> updateReceiptHistory (@RequestBody WaterReceiptHistoryDto request){
        return ResponseEntity.ok(waterReceiptService.updateReceiptHistory(request));
    }

}
