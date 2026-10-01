package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.AvisoResponsablePagoEntregaRequestDto;
import com.mx.uvas.watersystem.dto.AvisoResponsablePagoGenerarRequestDto;
import com.mx.uvas.watersystem.response.AvisoResponsablePagoRestResponse;
import com.mx.uvas.watersystem.response.CasaUsuarioCuotaRestResponse;
import com.mx.uvas.watersystem.services.impl.AvisoResponsablePagoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/avisoResponsablePago")
@AllArgsConstructor
@Slf4j
public class AvisoResponsablePagoController {

    private final AvisoResponsablePagoService avisoResponsablePagoService;

    // Usuarios ya dados de alta en esta casa, con su cuota vigente -- para
    // el selector "agregar usuario existente" del formulario.
    @GetMapping("/casa/{casaId}/usuarios")
    public ResponseEntity<CasaUsuarioCuotaRestResponse> usuariosDeLaCasa(@PathVariable Integer casaId) {
        return avisoResponsablePagoService.usuariosDeLaCasa(casaId);
    }

    @GetMapping("/historial")
    public ResponseEntity<AvisoResponsablePagoRestResponse> historial() {
        return avisoResponsablePagoService.historial();
    }

    // Historial de una casa en particular -- para el acordeón en la ficha
    // de la casa.
    @GetMapping("/casa/{casaId}/historial")
    public ResponseEntity<AvisoResponsablePagoRestResponse> historialPorCasa(@PathVariable Integer casaId) {
        return avisoResponsablePagoService.historialPorCasa(casaId);
    }

    @PutMapping("/{responsablePagoId}/entregar")
    public ResponseEntity<AvisoResponsablePagoRestResponse> marcarEntregada(
            @PathVariable Integer responsablePagoId,
            @RequestBody AvisoResponsablePagoEntregaRequestDto request) {
        return avisoResponsablePagoService.marcarEntregada(responsablePagoId, request);
    }

    @PutMapping("/{responsablePagoId}/cancelar")
    public ResponseEntity<AvisoResponsablePagoRestResponse> cancelar(@PathVariable Integer responsablePagoId) {
        return avisoResponsablePagoService.cancelar(responsablePagoId);
    }

    @PutMapping("/{responsablePagoId}/reactivar")
    public ResponseEntity<AvisoResponsablePagoRestResponse> reactivar(@PathVariable Integer responsablePagoId) {
        return avisoResponsablePagoService.reactivar(responsablePagoId);
    }

    // Regresa el PDF directo (no un BaseRestResponse en JSON) -- mismo
    // patrón que las demás cartas.
    @PostMapping("/generar")
    public ResponseEntity<byte[]> generar(@RequestBody AvisoResponsablePagoGenerarRequestDto request) {
        try {
            if (request.getCasaId() == null) {
                return errorResponse("Selecciona la casa");
            }

            AvisoResponsablePagoService.GenerarResponsablePagoResultado resultado = avisoResponsablePagoService.generar(request);

            if (!resultado.generado()) {
                return errorResponse(resultado.mensajeError() != null ? resultado.mensajeError() : "No se pudo generar el aviso");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("aviso_responsable_pago.pdf").build());
            return new ResponseEntity<>(resultado.pdfBytes(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al generar el aviso de responsables de pago", e);
            return errorResponse("Error al generar el aviso");
        }
    }

    private ResponseEntity<byte[]> errorResponse(String mensaje) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Message", mensaje);
        return new ResponseEntity<>(new byte[0], headers, HttpStatus.BAD_REQUEST);
    }
}
