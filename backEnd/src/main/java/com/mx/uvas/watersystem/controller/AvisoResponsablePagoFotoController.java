package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.response.AvisoResponsablePagoFotoRestResponse;
import com.mx.uvas.watersystem.services.IAvisoResponsablePagoFotoService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Fotos de respaldo de la entrega de un aviso de responsables de pago (ver
// AvisoResponsablePagoFotoService) -- mismo patrón que
// AvisoAdeudoFotoController.
@RestController
@RequestMapping(path = "/api/v1/avisoResponsablePago")
@AllArgsConstructor
public class AvisoResponsablePagoFotoController {

    private final IAvisoResponsablePagoFotoService avisoResponsablePagoFotoService;

    @PostMapping("/{responsablePagoId}/fotos")
    public ResponseEntity<AvisoResponsablePagoFotoRestResponse> subir(
            @PathVariable Integer responsablePagoId,
            @RequestParam("archivo") MultipartFile archivo) {
        return avisoResponsablePagoFotoService.subir(responsablePagoId, archivo);
    }

    @GetMapping("/{responsablePagoId}/fotos")
    public ResponseEntity<AvisoResponsablePagoFotoRestResponse> listar(@PathVariable Integer responsablePagoId) {
        return avisoResponsablePagoFotoService.listarPorAviso(responsablePagoId);
    }

    @DeleteMapping("/fotos/{fotoId}")
    public ResponseEntity<AvisoResponsablePagoFotoRestResponse> eliminar(@PathVariable Integer fotoId) {
        return avisoResponsablePagoFotoService.eliminar(fotoId);
    }

    // Sirve el archivo real -- sigue protegido por el mismo filtro JWT que
    // el resto de /api/v1/**, así que hay que mandar el Authorization al
    // pedir la imagen (el <img> del front la trae como blob, no con src
    // directo, precisamente por esto).
    @GetMapping("/fotos/{fotoId}/archivo")
    public ResponseEntity<Resource> archivo(@PathVariable Integer fotoId) {
        IAvisoResponsablePagoFotoService.ArchivoFoto archivoFoto = avisoResponsablePagoFotoService.obtenerArchivo(fotoId);
        if (archivoFoto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(archivoFoto.contentType())
                .body(archivoFoto.resource());
    }
}
