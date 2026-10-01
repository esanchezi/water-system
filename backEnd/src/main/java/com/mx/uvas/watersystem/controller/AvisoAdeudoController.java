package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.AvisoAdeudoAtencionRequestDto;
import com.mx.uvas.watersystem.dto.AvisoAdeudoCancelarRequestDto;
import com.mx.uvas.watersystem.dto.AvisoAdeudoEntregaRequestDto;
import com.mx.uvas.watersystem.dto.AvisoAdeudoGenerarRequestDto;
import com.mx.uvas.watersystem.response.AdeudoLuzUsuarioRestResponse;
import com.mx.uvas.watersystem.response.AvisoAdeudoRestResponse;
import com.mx.uvas.watersystem.services.impl.AvisoAdeudoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1/avisoAdeudo")
@AllArgsConstructor
@Slf4j
public class AvisoAdeudoController {

    private final AvisoAdeudoService avisoAdeudoService;

    @GetMapping("/candidatos")
    public ResponseEntity<AdeudoLuzUsuarioRestResponse> candidatos() {
        return avisoAdeudoService.candidatos();
    }

    // La pantalla ahora pide elegir zona y calle antes de calcular, para no
    // recorrer el adeudo de TODOS los usuarios activos cada vez que se abre.
    @GetMapping("/candidatos/porCalle/{calleId}")
    public ResponseEntity<AdeudoLuzUsuarioRestResponse> candidatosPorCalle(@PathVariable Integer calleId) {
        return avisoAdeudoService.candidatosPorCalle(calleId);
    }

    @GetMapping("/historial")
    public ResponseEntity<AvisoAdeudoRestResponse> historial() {
        return avisoAdeudoService.historial();
    }

    // Historial completo (activas + canceladas) de un usuario específico --
    // para el acordeón "Cartas generadas" en su ficha.
    @GetMapping("/porUsuario/{aguaUsuarioId}")
    public ResponseEntity<AvisoAdeudoRestResponse> porUsuario(@PathVariable Integer aguaUsuarioId) {
        return avisoAdeudoService.porUsuario(aguaUsuarioId);
    }

    // Para el botón "Generar Segundo aviso" directo desde una fila del
    // historial -- recalcula el estado ACTUAL de este usuario (no la fila
    // del historial, que es un snapshot viejo) y regresa si de verdad le
    // toca el Segundo aviso ahora mismo.
    @GetMapping("/candidatoUnico/{aguaUsuarioId}")
    public ResponseEntity<AdeudoLuzUsuarioRestResponse> candidatoUnico(@PathVariable Integer aguaUsuarioId) {
        return avisoAdeudoService.candidatoUnico(aguaUsuarioId);
    }

    // Registra la entrega (o el intento de entrega) de una carta ya
    // generada -- replica la sección "RAZÓN DE NOTIFICACIÓN" de la carta física.
    @PutMapping("/{avisoAdeudoId}/entregar")
    public ResponseEntity<AvisoAdeudoRestResponse> marcarEntregada(
            @PathVariable Integer avisoAdeudoId,
            @RequestBody AvisoAdeudoEntregaRequestDto request) {
        return avisoAdeudoService.marcarEntregada(avisoAdeudoId, request);
    }

    // Body opcional -- @RequestBody(required = false) para no romper
    // llamadas viejas del frontend que cancelaban sin mandar nada.
    @PutMapping("/{avisoAdeudoId}/cancelar")
    public ResponseEntity<AvisoAdeudoRestResponse> cancelar(
            @PathVariable Integer avisoAdeudoId,
            @RequestBody(required = false) AvisoAdeudoCancelarRequestDto request) {
        return avisoAdeudoService.cancelar(avisoAdeudoId, request);
    }

    @PutMapping("/{avisoAdeudoId}/reactivar")
    public ResponseEntity<AvisoAdeudoRestResponse> reactivar(@PathVariable Integer avisoAdeudoId) {
        return avisoAdeudoService.reactivar(avisoAdeudoId);
    }

    // Cartas ya entregadas a este usuario que aún no se marcan como
    // atendidas -- para la alerta al consultar su ficha.
    @GetMapping("/pendientesDeAtencion/{aguaUsuarioId}")
    public ResponseEntity<AvisoAdeudoRestResponse> pendientesDeAtencion(@PathVariable Integer aguaUsuarioId) {
        return avisoAdeudoService.pendientesDeAtencion(aguaUsuarioId);
    }

    @PutMapping("/{avisoAdeudoId}/atender")
    public ResponseEntity<AvisoAdeudoRestResponse> marcarAtendida(
            @PathVariable Integer avisoAdeudoId,
            @RequestBody AvisoAdeudoAtencionRequestDto request) {
        return avisoAdeudoService.marcarAtendida(avisoAdeudoId, request);
    }

    // Regresa el PDF directo (no un BaseRestResponse en JSON) -- por eso los
    // avisos ("se omitieron estos usuarios porque ya no tienen adeudo", o el
    // motivo de un error) van en headers propios en vez de en el cuerpo. Ver
    // SecurityConfig.corsConfigurationSource() para que el navegador los deje
    // leer desde el frontend.
    @PostMapping("/generar")
    public ResponseEntity<byte[]> generar(@RequestBody AvisoAdeudoGenerarRequestDto request) {
        try {
            boolean sinRegistrados = request.getAguaUsuarioIds() == null || request.getAguaUsuarioIds().isEmpty();
            boolean sinNoRegistrados = request.getNoRegistrados() == null || request.getNoRegistrados().isEmpty();
            if (sinRegistrados && sinNoRegistrados) {
                return errorResponse("Selecciona al menos un usuario, o agrega una persona no registrada");
            }

            AvisoAdeudoService.GenerarAvisosResultado resultado = avisoAdeudoService.generar(request);

            if (resultado.totalGeneradas() == 0) {
                return errorResponse("Ninguno de los usuarios seleccionados tiene adeudo de luz pendiente");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("avisos_adeudo.pdf").build());
            if (!resultado.noUsuariosOmitidos().isEmpty()) {
                headers.add("X-Usuarios-Omitidos", listaComoTexto(resultado.noUsuariosOmitidos()));
            }
            return new ResponseEntity<>(resultado.pdfBytes(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al generar avisos de adeudo", e);
            return errorResponse("Error al generar las cartas");
        }
    }

    private String listaComoTexto(List<Integer> valores) {
        return valores.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private ResponseEntity<byte[]> errorResponse(String mensaje) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Message", mensaje);
        return new ResponseEntity<>(new byte[0], headers, HttpStatus.BAD_REQUEST);
    }
}
