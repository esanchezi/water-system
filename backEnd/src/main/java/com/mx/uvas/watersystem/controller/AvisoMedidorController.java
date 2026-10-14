package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.AvisoMedidorEntregaRequestDto;
import com.mx.uvas.watersystem.dto.AvisoMedidorGenerarRequestDto;
import com.mx.uvas.watersystem.response.AvisoMedidorRestResponse;
import com.mx.uvas.watersystem.services.impl.AvisoMedidorService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/avisoMedidor")
@AllArgsConstructor
@Slf4j
public class AvisoMedidorController {

    private final AvisoMedidorService avisoMedidorService;

    @GetMapping("/historial")
    public ResponseEntity<AvisoMedidorRestResponse> historial() {
        return avisoMedidorService.historial();
    }

    // Historial completo de un usuario específico -- para el acordeón
    // "Cartas generadas" en su ficha.
    @GetMapping("/porUsuario/{aguaUsuarioId}")
    public ResponseEntity<AvisoMedidorRestResponse> porUsuario(@PathVariable Integer aguaUsuarioId) {
        return avisoMedidorService.porUsuario(aguaUsuarioId);
    }

    @PutMapping("/{avisoMedidorId}/entregar")
    public ResponseEntity<AvisoMedidorRestResponse> marcarEntregada(
            @PathVariable Integer avisoMedidorId,
            @RequestBody AvisoMedidorEntregaRequestDto request) {
        return avisoMedidorService.marcarEntregada(avisoMedidorId, request);
    }

    @PutMapping("/{avisoMedidorId}/cancelar")
    public ResponseEntity<AvisoMedidorRestResponse> cancelar(@PathVariable Integer avisoMedidorId) {
        return avisoMedidorService.cancelar(avisoMedidorId);
    }

    @PutMapping("/{avisoMedidorId}/reactivar")
    public ResponseEntity<AvisoMedidorRestResponse> reactivar(@PathVariable Integer avisoMedidorId) {
        return avisoMedidorService.reactivar(avisoMedidorId);
    }

    // Regresa el PDF directo (no un BaseRestResponse en JSON) -- mismo
    // patrón que el resto de los controladores de avisos.
    @PostMapping("/generar")
    public ResponseEntity<byte[]> generar(@RequestBody AvisoMedidorGenerarRequestDto request) {
        try {
            if (request.getAguaUsuarioId() == null) {
                return errorResponse("Selecciona el usuario/domicilio de prueba");
            }
            if (request.getFechaInstalacion() == null) {
                return errorResponse("Indica la fecha de instalación");
            }
            if (request.getNoSerieMedidor() == null || request.getNoSerieMedidor().isBlank()) {
                return errorResponse("Indica el número de serie del medidor");
            }

            AvisoMedidorService.GenerarAvisoMedidorResultado resultado = avisoMedidorService.generar(request);

            if (!resultado.generado()) {
                return errorResponse("No se pudo generar el aviso para el usuario seleccionado");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("aviso_instalacion_medidor.pdf").build());
            return new ResponseEntity<>(resultado.pdfBytes(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al generar el aviso de instalación de medidor", e);
            return errorResponse("Error al generar el aviso");
        }
    }

    private ResponseEntity<byte[]> errorResponse(String mensaje) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Message", mensaje);
        return new ResponseEntity<>(new byte[0], headers, HttpStatus.BAD_REQUEST);
    }
}
