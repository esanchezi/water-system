package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.AvisoInformativoAdeudoEntregaRequestDto;
import com.mx.uvas.watersystem.dto.AvisoInformativoAdeudoGenerarRequestDto;
import com.mx.uvas.watersystem.response.AvisoInformativoAdeudoRestResponse;
import com.mx.uvas.watersystem.services.impl.AvisoInformativoAdeudoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/avisoInformativoAdeudo")
@AllArgsConstructor
@Slf4j
public class AvisoInformativoAdeudoController {

    private final AvisoInformativoAdeudoService avisoInformativoAdeudoService;

    @GetMapping("/historial")
    public ResponseEntity<AvisoInformativoAdeudoRestResponse> historial() {
        return avisoInformativoAdeudoService.historial();
    }

    // Historial completo de un usuario específico -- para el acordeón
    // "Cartas generadas" en su ficha.
    @GetMapping("/porUsuario/{aguaUsuarioId}")
    public ResponseEntity<AvisoInformativoAdeudoRestResponse> porUsuario(@PathVariable Integer aguaUsuarioId) {
        return avisoInformativoAdeudoService.porUsuario(aguaUsuarioId);
    }

    @PutMapping("/{avisoInformativoAdeudoId}/entregar")
    public ResponseEntity<AvisoInformativoAdeudoRestResponse> marcarEntregada(
            @PathVariable Integer avisoInformativoAdeudoId,
            @RequestBody AvisoInformativoAdeudoEntregaRequestDto request) {
        return avisoInformativoAdeudoService.marcarEntregada(avisoInformativoAdeudoId, request);
    }

    @PutMapping("/{avisoInformativoAdeudoId}/cancelar")
    public ResponseEntity<AvisoInformativoAdeudoRestResponse> cancelar(@PathVariable Integer avisoInformativoAdeudoId) {
        return avisoInformativoAdeudoService.cancelar(avisoInformativoAdeudoId);
    }

    @PutMapping("/{avisoInformativoAdeudoId}/reactivar")
    public ResponseEntity<AvisoInformativoAdeudoRestResponse> reactivar(@PathVariable Integer avisoInformativoAdeudoId) {
        return avisoInformativoAdeudoService.reactivar(avisoInformativoAdeudoId);
    }

    // Regresa el PDF directo, mismo patrón que AvisoBombaController.generar().
    @PostMapping("/generar")
    public ResponseEntity<byte[]> generar(@RequestBody AvisoInformativoAdeudoGenerarRequestDto request) {
        try {
            if (request.getUsuarios() == null || request.getUsuarios().isEmpty()) {
                return errorResponse("Selecciona al menos un usuario");
            }

            AvisoInformativoAdeudoService.GenerarAvisosInformativosResultado resultado = avisoInformativoAdeudoService.generar(request);

            if (resultado.totalGeneradas() == 0) {
                return errorResponse("No se pudo generar ningún aviso para los usuarios seleccionados");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("avisos_informativos_adeudo.pdf").build());
            return new ResponseEntity<>(resultado.pdfBytes(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al generar avisos informativos de adeudo", e);
            return errorResponse("Error al generar los avisos");
        }
    }

    private ResponseEntity<byte[]> errorResponse(String mensaje) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Message", mensaje);
        return new ResponseEntity<>(new byte[0], headers, HttpStatus.BAD_REQUEST);
    }
}
