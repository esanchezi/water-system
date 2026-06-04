package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.WaterGroupDto;
import com.mx.uvas.watersystem.response.WaterGroupRestResponse;
import com.mx.uvas.watersystem.services.IWaterGroupService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
//@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping(path = "/api/v1/waterGroup" )
@AllArgsConstructor
public class WaterGroupController {
    private final IWaterGroupService waterGroupService;
    @GetMapping("/")
    public ResponseEntity<WaterGroupRestResponse> getListWaterGroup() {
        return waterGroupService.findAll();
    }

    @PostMapping(path = "/")
    public ResponseEntity<WaterGroupRestResponse> create(@RequestBody WaterGroupDto dto) {
        return waterGroupService.createWaterGroup(dto);
    }

}
