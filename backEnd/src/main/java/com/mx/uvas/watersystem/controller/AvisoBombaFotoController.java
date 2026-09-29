package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.response.AvisoBombaFotoRestResponse;
import com.mx.uvas.watersystem.services.IAvisoBombaFotoService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Fotos de respaldo de la entrega de un aviso de uso de bomba (ver
// AvisoBombaFotoService) -- mismo patrón que AvisoAdeudoFotoController.
@RestController
@RequestMapping(path = "/api/v1/avisoBomba")
@AllArgsConstructor
public class AvisoBombaFotoController {

    private final IAvisoBombaFotoService avisoBombaFotoService;

    @PostMapping("/{avisoBombaId}/fotos")
    public ResponseEntity<AvisoBombaFotoRestResponse> subir(
            @PathVariable Integer avisoBombaId,
            @RequestParam("archivo") MultipartFile archivo) {
        return avisoBombaFotoService.subir(avisoBombaId, archivo);
    }

    @GetMapping("/{avisoBombaId}/fotos")
    public ResponseEntity<AvisoBombaFotoRestResponse> listar(@PathVariable Integer avisoBombaId) {
        return avisoBombaFotoService.listarPorAviso(avisoBombaId);
    }

    @DeleteMapping("/fotos/{fotoId}")
    public ResponseEntity<AvisoBombaFotoRestResponse> eliminar(@PathVariable Integer fotoId) {
        return avisoBombaFotoService.eliminar(fotoId);
    }

    // Sirve el archivo real -- sigue protegido por el mismo filtro JWT que
    // el resto de /api/v1/**, así que hay que mandar el Authorization al
    // pedir la imagen (el <img> del front la trae como blob, no con src
    // directo, precisamente por esto).
    @GetMapping("/fotos/{fotoId}/archivo")
    public ResponseEntity<Resource> archivo(@PathVariable Integer fotoId) {
        IAvisoBombaFotoService.ArchivoFoto archivoFoto = avisoBombaFotoService.obtenerArchivo(fotoId);
        if (archivoFoto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(archivoFoto.contentType())
                .body(archivoFoto.resource());
    }
}
