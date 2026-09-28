package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.response.AvisoAdeudoFotoRestResponse;
import com.mx.uvas.watersystem.services.IAvisoAdeudoFotoService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Fotos de respaldo de la entrega de una carta de adeudo (ver
// AvisoAdeudoFotoService) -- mismo patrón que ValvulaFotoController.
@RestController
@RequestMapping(path = "/api/v1/avisoAdeudo")
@AllArgsConstructor
public class AvisoAdeudoFotoController {

    private final IAvisoAdeudoFotoService avisoAdeudoFotoService;

    @PostMapping("/{avisoAdeudoId}/fotos")
    public ResponseEntity<AvisoAdeudoFotoRestResponse> subir(
            @PathVariable Integer avisoAdeudoId,
            @RequestParam("archivo") MultipartFile archivo) {
        return avisoAdeudoFotoService.subir(avisoAdeudoId, archivo);
    }

    @GetMapping("/{avisoAdeudoId}/fotos")
    public ResponseEntity<AvisoAdeudoFotoRestResponse> listar(@PathVariable Integer avisoAdeudoId) {
        return avisoAdeudoFotoService.listarPorAviso(avisoAdeudoId);
    }

    @DeleteMapping("/fotos/{fotoId}")
    public ResponseEntity<AvisoAdeudoFotoRestResponse> eliminar(@PathVariable Integer fotoId) {
        return avisoAdeudoFotoService.eliminar(fotoId);
    }

    // Sirve el archivo real -- sigue protegido por el mismo filtro JWT que
    // el resto de /api/v1/**, así que hay que mandar el Authorization al
    // pedir la imagen (el <img> del front la trae como blob, no con src
    // directo, precisamente por esto).
    @GetMapping("/fotos/{fotoId}/archivo")
    public ResponseEntity<Resource> archivo(@PathVariable Integer fotoId) {
        IAvisoAdeudoFotoService.ArchivoFoto archivoFoto = avisoAdeudoFotoService.obtenerArchivo(fotoId);
        if (archivoFoto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(archivoFoto.contentType())
                .body(archivoFoto.resource());
    }
}
