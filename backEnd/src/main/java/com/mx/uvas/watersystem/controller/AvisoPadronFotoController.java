package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.response.AvisoPadronFotoRestResponse;
import com.mx.uvas.watersystem.services.IAvisoPadronFotoService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Fotos de respaldo de la entrega de un aviso de actualización de padrón
// (ver AvisoPadronFotoService) -- mismo patrón que AvisoAdeudoFotoController.
@RestController
@RequestMapping(path = "/api/v1/avisoPadron")
@AllArgsConstructor
public class AvisoPadronFotoController {

    private final IAvisoPadronFotoService avisoPadronFotoService;

    @PostMapping("/{avisoPadronId}/fotos")
    public ResponseEntity<AvisoPadronFotoRestResponse> subir(
            @PathVariable Integer avisoPadronId,
            @RequestParam("archivo") MultipartFile archivo) {
        return avisoPadronFotoService.subir(avisoPadronId, archivo);
    }

    @GetMapping("/{avisoPadronId}/fotos")
    public ResponseEntity<AvisoPadronFotoRestResponse> listar(@PathVariable Integer avisoPadronId) {
        return avisoPadronFotoService.listarPorAviso(avisoPadronId);
    }

    @DeleteMapping("/fotos/{fotoId}")
    public ResponseEntity<AvisoPadronFotoRestResponse> eliminar(@PathVariable Integer fotoId) {
        return avisoPadronFotoService.eliminar(fotoId);
    }

    // Sirve el archivo real -- sigue protegido por el mismo filtro JWT que
    // el resto de /api/v1/**, así que hay que mandar el Authorization al
    // pedir la imagen (el <img> del front la trae como blob, no con src
    // directo, precisamente por esto).
    @GetMapping("/fotos/{fotoId}/archivo")
    public ResponseEntity<Resource> archivo(@PathVariable Integer fotoId) {
        IAvisoPadronFotoService.ArchivoFoto archivoFoto = avisoPadronFotoService.obtenerArchivo(fotoId);
        if (archivoFoto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(archivoFoto.contentType())
                .body(archivoFoto.resource());
    }
}
