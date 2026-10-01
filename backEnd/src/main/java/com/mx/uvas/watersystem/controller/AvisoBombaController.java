package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.AvisoBombaEntregaRequestDto;
import com.mx.uvas.watersystem.dto.AvisoBombaGenerarRequestDto;
import com.mx.uvas.watersystem.response.AvisoBombaCandidatoRestResponse;
import com.mx.uvas.watersystem.response.AvisoBombaRestResponse;
import com.mx.uvas.watersystem.services.impl.AvisoBombaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/avisoBomba")
@AllArgsConstructor
@Slf4j
public class AvisoBombaController {

    private final AvisoBombaService avisoBombaService;

    @GetMapping("/candidatos/porCalle/{calleId}")
    public ResponseEntity<AvisoBombaCandidatoRestResponse> candidatosPorCalle(@PathVariable Integer calleId) {
        return avisoBombaService.candidatosPorCalle(calleId);
    }

    @GetMapping("/historial")
    public ResponseEntity<AvisoBombaRestResponse> historial() {
        return avisoBombaService.historial();
    }

    // Historial completo de un usuario específico -- para el acordeón
    // "Cartas generadas" en su ficha.
    @GetMapping("/porUsuario/{aguaUsuarioId}")
    public ResponseEntity<AvisoBombaRestResponse> porUsuario(@PathVariable Integer aguaUsuarioId) {
        return avisoBombaService.porUsuario(aguaUsuarioId);
    }

    @PutMapping("/{avisoBombaId}/entregar")
    public ResponseEntity<AvisoBombaRestResponse> marcarEntregada(
            @PathVariable Integer avisoBombaId,
            @RequestBody AvisoBombaEntregaRequestDto request) {
        return avisoBombaService.marcarEntregada(avisoBombaId, request);
    }

    @PutMapping("/{avisoBombaId}/cancelar")
    public ResponseEntity<AvisoBombaRestResponse> cancelar(@PathVariable Integer avisoBombaId) {
        return avisoBombaService.cancelar(avisoBombaId);
    }

    @PutMapping("/{avisoBombaId}/reactivar")
    public ResponseEntity<AvisoBombaRestResponse> reactivar(@PathVariable Integer avisoBombaId) {
        return avisoBombaService.reactivar(avisoBombaId);
    }

    // Regresa el PDF directo (no un BaseRestResponse en JSON) -- mismo
    // patrón que AvisoAdeudoController.generar().
    @PostMapping("/generar")
    public ResponseEntity<byte[]> generar(@RequestBody AvisoBombaGenerarRequestDto request) {
        try {
            if (request.getAguaUsuarioIds() == null || request.getAguaUsuarioIds().isEmpty()) {
                return errorResponse("Selecciona al menos un usuario");
            }
            if (request.getFechaReporte() == null) {
                return errorResponse("Indica la fecha del reporte / revisión");
            }

            AvisoBombaService.GenerarAvisosBombaResultado resultado = avisoBombaService.generar(request);

            if (resultado.totalGeneradas() == 0) {
                return errorResponse("No se pudo generar ningún aviso para los usuarios seleccionados");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("avisos_bomba.pdf").build());
            return new ResponseEntity<>(resultado.pdfBytes(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al generar avisos de uso de bomba", e);
            return errorResponse("Error al generar los avisos");
        }
    }

    private ResponseEntity<byte[]> errorResponse(String mensaje) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Message", mensaje);
        return new ResponseEntity<>(new byte[0], headers, HttpStatus.BAD_REQUEST);
    }
}
