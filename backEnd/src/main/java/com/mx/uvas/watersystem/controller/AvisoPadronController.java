package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.AvisoPadronEntregaRequestDto;
import com.mx.uvas.watersystem.dto.AvisoPadronGenerarRequestDto;
import com.mx.uvas.watersystem.response.AvisoPadronCandidatoRestResponse;
import com.mx.uvas.watersystem.response.AvisoPadronRestResponse;
import com.mx.uvas.watersystem.services.impl.AvisoPadronService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/avisoPadron")
@AllArgsConstructor
@Slf4j
public class AvisoPadronController {

    private final AvisoPadronService avisoPadronService;

    @GetMapping("/candidatos/porCalle/{calleId}")
    public ResponseEntity<AvisoPadronCandidatoRestResponse> candidatosPorCalle(@PathVariable Integer calleId) {
        return avisoPadronService.candidatosPorCalle(calleId);
    }

    @GetMapping("/historial")
    public ResponseEntity<AvisoPadronRestResponse> historial() {
        return avisoPadronService.historial();
    }

    @PutMapping("/{avisoPadronId}/entregar")
    public ResponseEntity<AvisoPadronRestResponse> marcarEntregada(
            @PathVariable Integer avisoPadronId,
            @RequestBody AvisoPadronEntregaRequestDto request) {
        return avisoPadronService.marcarEntregada(avisoPadronId, request);
    }

    @PutMapping("/{avisoPadronId}/cancelar")
    public ResponseEntity<AvisoPadronRestResponse> cancelar(@PathVariable Integer avisoPadronId) {
        return avisoPadronService.cancelar(avisoPadronId);
    }

    @PutMapping("/{avisoPadronId}/reactivar")
    public ResponseEntity<AvisoPadronRestResponse> reactivar(@PathVariable Integer avisoPadronId) {
        return avisoPadronService.reactivar(avisoPadronId);
    }

    // Regresa el PDF directo (no un BaseRestResponse en JSON) -- mismo
    // patrón que AvisoAdeudoController.generar()/AvisoBombaController.generar().
    @PostMapping("/generar")
    public ResponseEntity<byte[]> generar(@RequestBody AvisoPadronGenerarRequestDto request) {
        try {
            if (request.getAguaUsuarioIds() == null || request.getAguaUsuarioIds().isEmpty()) {
                return errorResponse("Selecciona al menos un usuario");
            }
            if (request.getFechaPresentacion() == null) {
                return errorResponse("Indica la fecha en que debe presentarse");
            }

            AvisoPadronService.GenerarAvisosPadronResultado resultado = avisoPadronService.generar(request);

            if (resultado.totalGeneradas() == 0) {
                return errorResponse("No se pudo generar ningún aviso para los usuarios seleccionados");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("avisos_padron.pdf").build());
            return new ResponseEntity<>(resultado.pdfBytes(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al generar avisos de actualización de padrón", e);
            return errorResponse("Error al generar los avisos");
        }
    }

    private ResponseEntity<byte[]> errorResponse(String mensaje) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Message", mensaje);
        return new ResponseEntity<>(new byte[0], headers, HttpStatus.BAD_REQUEST);
    }
}
