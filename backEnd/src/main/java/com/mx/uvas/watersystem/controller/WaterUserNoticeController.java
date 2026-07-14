package com.mx.uvas.watersystem.controller;


import com.mx.uvas.watersystem.dto.WaterUserNoticeDto;
import com.mx.uvas.watersystem.response.WaterUserNoticeRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserNoticeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/waterUserNotice" )
@AllArgsConstructor
public class WaterUserNoticeController {

    private final IWaterUserNoticeService waterUserNoticeService;

    @GetMapping("/{noUser}")
    public ResponseEntity<WaterUserNoticeRestResponse> searchNoticeByUser(@PathVariable Integer noUser) {
        return waterUserNoticeService.findByNoUser(noUser);
    }

    @PostMapping("/")
    public ResponseEntity<WaterUserNoticeRestResponse> create(@RequestBody WaterUserNoticeDto request) {
        return waterUserNoticeService.create(request);
    }

}
