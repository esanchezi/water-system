package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.RenunciaTemporalCrearRequestDto;
import com.mx.uvas.watersystem.dto.RenunciaTemporalReconexionRequestDto;
import com.mx.uvas.watersystem.response.RenunciaTemporalRestResponse;
import com.mx.uvas.watersystem.services.impl.RenunciaTemporalService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/renunciaTemporal")
@AllArgsConstructor
@Slf4j
public class RenunciaTemporalController {

    private final RenunciaTemporalService renunciaTemporalService;

    // Regresa el PDF directo, mismo patrón que /avisoAdeudo/generar -- el
    // frontend descarga el documento y por separado puede consultar el
    // historial (que ya trae el registro recién guardado).
    @PostMapping("/generar")
    public ResponseEntity<byte[]> generar(@RequestBody RenunciaTemporalCrearRequestDto request) {
        try {
            if (request.getAguaUsuarioId() == null) {
                return errorResponse("Selecciona un usuario");
            }
            RenunciaTemporalService.RenunciaGenerada resultado = renunciaTemporalService.generar(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("renuncia_temporal.pdf").build());
            return new ResponseEntity<>(resultado.pdfBytes(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al generar la renuncia temporal", e);
            return errorResponse("Error al generar la solicitud de renuncia temporal");
        }
    }

    @GetMapping("/historial/{aguaUsuarioId}")
    public ResponseEntity<RenunciaTemporalRestResponse> historialPorUsuario(@PathVariable Integer aguaUsuarioId) {
        return renunciaTemporalService.historialPorUsuario(aguaUsuarioId);
    }

    @PutMapping("/{renunciaTemporalId}/reconectar")
    public ResponseEntity<RenunciaTemporalRestResponse> reconectar(
            @PathVariable Integer renunciaTemporalId,
            @RequestBody RenunciaTemporalReconexionRequestDto request) {
        return renunciaTemporalService.reconectar(renunciaTemporalId, request);
    }

    @PutMapping("/{renunciaTemporalId}/cancelar")
    public ResponseEntity<RenunciaTemporalRestResponse> cancelar(@PathVariable Integer renunciaTemporalId) {
        return renunciaTemporalService.cancelar(renunciaTemporalId);
    }

    @GetMapping("/activaPorUsuario/{aguaUsuarioId}")
    public ResponseEntity<Boolean> activaPorUsuario(@PathVariable Integer aguaUsuarioId) {
        return ResponseEntity.ok(renunciaTemporalService.estaEnRenunciaTemporal(aguaUsuarioId));
    }

    private ResponseEntity<byte[]> errorResponse(String mensaje) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Message", mensaje);
        return new ResponseEntity<>(new byte[0], headers, HttpStatus.BAD_REQUEST);
    }
}
