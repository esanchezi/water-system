package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.response.ValvulaFotoRestResponse;
import com.mx.uvas.watersystem.services.IValvulaFotoService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/valvula")
@AllArgsConstructor
public class ValvulaFotoController {

    private final IValvulaFotoService valvulaFotoService;

    @PostMapping("/{valvulaId}/fotos")
    public ResponseEntity<ValvulaFotoRestResponse> subir(
            @PathVariable Integer valvulaId,
            @RequestParam("archivo") MultipartFile archivo) {
        return valvulaFotoService.subir(valvulaId, archivo);
    }

    @GetMapping("/{valvulaId}/fotos")
    public ResponseEntity<ValvulaFotoRestResponse> listar(@PathVariable Integer valvulaId) {
        return valvulaFotoService.listarPorValvula(valvulaId);
    }

    @DeleteMapping("/fotos/{fotoId}")
    public ResponseEntity<ValvulaFotoRestResponse> eliminar(@PathVariable Integer fotoId) {
        return valvulaFotoService.eliminar(fotoId);
    }

    // Sirve el archivo real -- sigue protegido por el mismo filtro JWT que
    // el resto de /api/v1/**, así que hay que mandar el Authorization al
    // pedir la imagen (el <img> del front la trae como blob, no con src
    // directo, precisamente por esto).
    @GetMapping("/fotos/{fotoId}/archivo")
    public ResponseEntity<Resource> archivo(@PathVariable Integer fotoId) {
        IValvulaFotoService.ArchivoFoto archivoFoto = valvulaFotoService.obtenerArchivo(fotoId);
        if (archivoFoto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(archivoFoto.contentType())
                .body(archivoFoto.resource());
    }
}
