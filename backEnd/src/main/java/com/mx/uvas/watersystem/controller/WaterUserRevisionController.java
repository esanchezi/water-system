package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.WaterUserRevisionRequestDto;
import com.mx.uvas.watersystem.response.WaterUserRevisionRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserRevisionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/waterUserRevision")
@AllArgsConstructor
public class WaterUserRevisionController {

    private final IWaterUserRevisionService waterUserRevisionService;

    @PostMapping("/{aguaUsuarioId}")
    public ResponseEntity<WaterUserRevisionRestResponse> registrar(@PathVariable Integer aguaUsuarioId,
                                                                     @RequestBody WaterUserRevisionRequestDto request) {
        return waterUserRevisionService.registrar(aguaUsuarioId, request);
    }

    @GetMapping("/porUsuario/{aguaUsuarioId}")
    public ResponseEntity<WaterUserRevisionRestResponse> historialPorUsuario(@PathVariable Integer aguaUsuarioId) {
        return waterUserRevisionService.historialPorUsuario(aguaUsuarioId);
    }

    @DeleteMapping("/{revisionId}")
    public ResponseEntity<WaterUserRevisionRestResponse> eliminar(@PathVariable Integer revisionId) {
        return waterUserRevisionService.eliminar(revisionId);
    }
}
