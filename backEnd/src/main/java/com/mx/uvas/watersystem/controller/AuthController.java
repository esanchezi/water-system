package com.mx.uvas.watersystem.controller;

import com.mx.uvas.watersystem.dto.CambiarPasswordDto;
import com.mx.uvas.watersystem.dto.LoginRequestDto;
import com.mx.uvas.watersystem.services.impl.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@RestController
@RequestMapping(path = "/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login: usuario/contraseña -> token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @Operation(summary = "Cambiar la contraseña del usuario logueado")
    @PutMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(Authentication authentication, @Valid @RequestBody CambiarPasswordDto request) {
        return authService.cambiarPassword(authentication.getName(), request);
    }
}
