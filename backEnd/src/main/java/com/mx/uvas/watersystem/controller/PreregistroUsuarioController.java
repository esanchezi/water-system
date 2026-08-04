package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.PreregistroUsuarioDto;
import com.mx.uvas.watersystem.response.PreregistroUsuarioRestResponse;
import com.mx.uvas.watersystem.services.IPreregistroUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/preregistro")
@AllArgsConstructor
public class PreregistroUsuarioController {

    private final IPreregistroUsuarioService preregistroUsuarioService;

    @Operation(summary = "Lista los preregistros de una casa")
    @GetMapping("/casa/{casaId}")
    public ResponseEntity<PreregistroUsuarioRestResponse> findByCasaId(@PathVariable Integer casaId) {
        return preregistroUsuarioService.findByCasaId(casaId);
    }

    @Operation(summary = "Agrega una persona en preregistro a una casa")
    @PostMapping("/{casaId}")
    public ResponseEntity<PreregistroUsuarioRestResponse> create(@PathVariable Integer casaId,
                                                                   @Valid @RequestBody PreregistroUsuarioDto request) {
        return preregistroUsuarioService.create(casaId, request);
    }

    @Operation(summary = "Edita nombre/teléfono/observaciones de un preregistro")
    @PutMapping("/{preregistroId}")
    public ResponseEntity<PreregistroUsuarioRestResponse> update(@PathVariable Integer preregistroId,
                                                                   @Valid @RequestBody PreregistroUsuarioDto request) {
        return preregistroUsuarioService.update(preregistroId, request);
    }

    @Operation(summary = "Marca el preregistro como convertido a un usuario real")
    @PutMapping("/{preregistroId}/convertir/{aguaUsuarioId}")
    public ResponseEntity<PreregistroUsuarioRestResponse> marcarConvertido(@PathVariable Integer preregistroId,
                                                                             @PathVariable Integer aguaUsuarioId) {
        return preregistroUsuarioService.marcarConvertido(preregistroId, aguaUsuarioId);
    }

    @Operation(summary = "Descarta el preregistro (no se va a convertir en usuario)")
    @PutMapping("/{preregistroId}/descartar")
    public ResponseEntity<PreregistroUsuarioRestResponse> marcarDescartado(@PathVariable Integer preregistroId,
                                                                             @RequestParam(required = false) String motivo) {
        return preregistroUsuarioService.marcarDescartado(preregistroId, motivo);
    }

}
