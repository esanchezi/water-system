package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.response.WaterUserRevisionFotoRestResponse;
import com.mx.uvas.watersystem.services.IWaterUserRevisionFotoService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/waterUserRevision")
@AllArgsConstructor
public class WaterUserRevisionFotoController {

    private final IWaterUserRevisionFotoService waterUserRevisionFotoService;

    @PostMapping("/{revisionId}/fotos")
    public ResponseEntity<WaterUserRevisionFotoRestResponse> subir(
            @PathVariable Integer revisionId,
            @RequestParam("archivo") MultipartFile archivo) {
        return waterUserRevisionFotoService.subir(revisionId, archivo);
    }

    @GetMapping("/{revisionId}/fotos")
    public ResponseEntity<WaterUserRevisionFotoRestResponse> listar(@PathVariable Integer revisionId) {
        return waterUserRevisionFotoService.listarPorRevision(revisionId);
    }

    @DeleteMapping("/fotos/{fotoId}")
    public ResponseEntity<WaterUserRevisionFotoRestResponse> eliminar(@PathVariable Integer fotoId) {
        return waterUserRevisionFotoService.eliminar(fotoId);
    }

    @GetMapping("/fotos/{fotoId}/archivo")
    public ResponseEntity<Resource> archivo(@PathVariable Integer fotoId) {
        IWaterUserRevisionFotoService.ArchivoFoto archivoFoto = waterUserRevisionFotoService.obtenerArchivo(fotoId);
        if (archivoFoto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(archivoFoto.contentType()).body(archivoFoto.resource());
    }
}
