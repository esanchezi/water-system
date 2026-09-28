package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.ResetPasswordDto;
import com.mx.uvas.watersystem.dto.SistemaUsuarioCreateDto;
import com.mx.uvas.watersystem.dto.SistemaUsuarioDto;
import com.mx.uvas.watersystem.response.SistemaUsuarioRestResponse;
import com.mx.uvas.watersystem.services.ISistemaUsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Administra cuentas de acceso (login), no usuarios del servicio de agua.
// Pensada para que la administradora pueda dar de alta cuentas con rol
// restringido (ej. "USUARIO1") ella misma. Por ahora, igual que el resto
// de /api/v1/**, solo exige estar logueada -- no hay todavía un candado
// de "solo ADMIN" en el servidor (se decidió dejarlo solo oculto del menú
// por ahora).
@RestController
@RequestMapping(path = "/api/v1/sistemaUsuario")
@AllArgsConstructor
public class SistemaUsuarioController {

    private final ISistemaUsuarioService sistemaUsuarioService;

    @GetMapping("/")
    public ResponseEntity<SistemaUsuarioRestResponse> getAll() {
        return sistemaUsuarioService.findAll();
    }

    @PostMapping("/")
    public ResponseEntity<SistemaUsuarioRestResponse> create(@RequestBody SistemaUsuarioCreateDto dto) {
        return sistemaUsuarioService.create(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SistemaUsuarioRestResponse> update(@PathVariable Integer id, @RequestBody SistemaUsuarioDto dto) {
        return sistemaUsuarioService.update(id, dto);
    }

    @PutMapping("/{id}/resetPassword")
    public ResponseEntity<SistemaUsuarioRestResponse> resetPassword(@PathVariable Integer id, @RequestBody ResetPasswordDto dto) {
        return sistemaUsuarioService.resetPassword(id, dto);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<SistemaUsuarioRestResponse> deactivate(@PathVariable Integer id) {
        return sistemaUsuarioService.deactivate(id);
    }
}
