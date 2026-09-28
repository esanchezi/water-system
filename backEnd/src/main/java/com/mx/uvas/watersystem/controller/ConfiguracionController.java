package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.ConfiguracionDto;
import com.mx.uvas.watersystem.response.ConfiguracionRestResponse;
import com.mx.uvas.watersystem.services.IConfiguracionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/configuracion")
@AllArgsConstructor
public class ConfiguracionController {

    private final IConfiguracionService configuracionService;

    @GetMapping("/")
    public ResponseEntity<ConfiguracionRestResponse> getAll() {
        return configuracionService.findAll();
    }

    @PutMapping("/{clave}")
    public ResponseEntity<ConfiguracionRestResponse> updateByClave(
            @PathVariable String clave,
            @RequestBody ConfiguracionDto dto) {
        return configuracionService.updateByClave(clave, dto);
    }
}
