package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.response.AvisoInformativoAdeudoFotoRestResponse;
import com.mx.uvas.watersystem.services.IAvisoInformativoAdeudoFotoService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Fotos de respaldo de la entrega de un Aviso Informativo de Adeudo -- mismo
// patrón que AvisoBombaFotoController.
@RestController
@RequestMapping(path = "/api/v1/avisoInformativoAdeudo")
@AllArgsConstructor
public class AvisoInformativoAdeudoFotoController {

    private final IAvisoInformativoAdeudoFotoService avisoInformativoAdeudoFotoService;

    @PostMapping("/{avisoInformativoAdeudoId}/fotos")
    public ResponseEntity<AvisoInformativoAdeudoFotoRestResponse> subir(
            @PathVariable Integer avisoInformativoAdeudoId,
            @RequestParam("archivo") MultipartFile archivo) {
        return avisoInformativoAdeudoFotoService.subir(avisoInformativoAdeudoId, archivo);
    }

    @GetMapping("/{avisoInformativoAdeudoId}/fotos")
    public ResponseEntity<AvisoInformativoAdeudoFotoRestResponse> listar(@PathVariable Integer avisoInformativoAdeudoId) {
        return avisoInformativoAdeudoFotoService.listarPorAviso(avisoInformativoAdeudoId);
    }

    @DeleteMapping("/fotos/{fotoId}")
    public ResponseEntity<AvisoInformativoAdeudoFotoRestResponse> eliminar(@PathVariable Integer fotoId) {
        return avisoInformativoAdeudoFotoService.eliminar(fotoId);
    }

    @GetMapping("/fotos/{fotoId}/archivo")
    public ResponseEntity<Resource> archivo(@PathVariable Integer fotoId) {
        IAvisoInformativoAdeudoFotoService.ArchivoFoto archivoFoto = avisoInformativoAdeudoFotoService.obtenerArchivo(fotoId);
        if (archivoFoto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(archivoFoto.contentType())
                .body(archivoFoto.resource());
    }
}
