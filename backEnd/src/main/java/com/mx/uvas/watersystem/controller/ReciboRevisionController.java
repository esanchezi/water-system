package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.ReciboRevisionCreateDto;
import com.mx.uvas.watersystem.dto.ReciboRevisionUpdateDto;
import com.mx.uvas.watersystem.response.ReciboRevisionFotoRestResponse;
import com.mx.uvas.watersystem.response.ReciboRevisionRestResponse;
import com.mx.uvas.watersystem.services.IReciboRevisionService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/reciboRevision")
@AllArgsConstructor
public class ReciboRevisionController {

    private final IReciboRevisionService reciboRevisionService;

    @PostMapping("/fotos")
    public ResponseEntity<ReciboRevisionFotoRestResponse> subirFoto(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "observaciones", required = false) String observaciones) {
        return reciboRevisionService.subirFoto(archivo, observaciones);
    }

    @GetMapping("/fotos")
    public ResponseEntity<ReciboRevisionFotoRestResponse> listarFotos() {
        return reciboRevisionService.listarFotos();
    }

    @DeleteMapping("/fotos/{fotoId}")
    public ResponseEntity<ReciboRevisionFotoRestResponse> eliminarFoto(@PathVariable Integer fotoId) {
        return reciboRevisionService.eliminarFoto(fotoId);
    }

    // Igual que en válvulas: protegido por el filtro JWT, así que el
    // frontend debe traerla como blob mandando el Authorization, no con
    // un <img src> directo.
    @GetMapping("/fotos/{fotoId}/archivo")
    public ResponseEntity<Resource> archivo(@PathVariable Integer fotoId) {
        IReciboRevisionService.ArchivoFoto archivoFoto = reciboRevisionService.obtenerArchivo(fotoId);
        if (archivoFoto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(archivoFoto.contentType())
                .body(archivoFoto.resource());
    }

    @PostMapping("/fotos/{fotoId}/revisiones")
    public ResponseEntity<ReciboRevisionRestResponse> agregarRevision(
            @PathVariable Integer fotoId,
            @RequestBody ReciboRevisionCreateDto dto) {
        return reciboRevisionService.agregarRevision(fotoId, dto);
    }

    @PutMapping("/revisiones/{revisionId}")
    public ResponseEntity<ReciboRevisionRestResponse> actualizarRevision(
            @PathVariable Integer revisionId,
            @RequestBody ReciboRevisionUpdateDto dto) {
        return reciboRevisionService.actualizarRevision(revisionId, dto);
    }

    @DeleteMapping("/revisiones/{revisionId}")
    public ResponseEntity<ReciboRevisionRestResponse> eliminarRevision(@PathVariable Integer revisionId) {
        return reciboRevisionService.eliminarRevision(revisionId);
    }
}
