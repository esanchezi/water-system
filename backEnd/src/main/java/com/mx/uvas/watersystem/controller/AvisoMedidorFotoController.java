package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.response.AvisoMedidorFotoRestResponse;
import com.mx.uvas.watersystem.services.IAvisoMedidorFotoService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Fotos de respaldo de la entrega de un aviso de instalación de medidor (ver
// AvisoMedidorFotoService) -- mismo patrón que AvisoBombaFotoController.
@RestController
@RequestMapping(path = "/api/v1/avisoMedidor")
@AllArgsConstructor
public class AvisoMedidorFotoController {

    private final IAvisoMedidorFotoService avisoMedidorFotoService;

    @PostMapping("/{avisoMedidorId}/fotos")
    public ResponseEntity<AvisoMedidorFotoRestResponse> subir(
            @PathVariable Integer avisoMedidorId,
            @RequestParam("archivo") MultipartFile archivo) {
        return avisoMedidorFotoService.subir(avisoMedidorId, archivo);
    }

    @GetMapping("/{avisoMedidorId}/fotos")
    public ResponseEntity<AvisoMedidorFotoRestResponse> listar(@PathVariable Integer avisoMedidorId) {
        return avisoMedidorFotoService.listarPorAviso(avisoMedidorId);
    }

    @DeleteMapping("/fotos/{fotoId}")
    public ResponseEntity<AvisoMedidorFotoRestResponse> eliminar(@PathVariable Integer fotoId) {
        return avisoMedidorFotoService.eliminar(fotoId);
    }

    // Sirve el archivo real -- sigue protegido por el mismo filtro JWT que
    // el resto de /api/v1/**, así que hay que mandar el Authorization al
    // pedir la imagen (el <img> del front la trae como blob, no con src
    // directo, precisamente por esto).
    @GetMapping("/fotos/{fotoId}/archivo")
    public ResponseEntity<Resource> archivo(@PathVariable Integer fotoId) {
        IAvisoMedidorFotoService.ArchivoFoto archivoFoto = avisoMedidorFotoService.obtenerArchivo(fotoId);
        if (archivoFoto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(archivoFoto.contentType())
                .body(archivoFoto.resource());
    }
}
