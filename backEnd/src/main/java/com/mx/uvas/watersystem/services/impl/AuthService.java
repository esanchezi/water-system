package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.CambiarPasswordDto;
import com.mx.uvas.watersystem.dto.LoginRequestDto;
import com.mx.uvas.watersystem.dto.LoginResponseDto;
import com.mx.uvas.watersystem.model.SistemaUsuarioEntity;
import com.mx.uvas.watersystem.repositories.ISistemaUsuarioRepository;
import com.mx.uvas.watersystem.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {

    private final ISistemaUsuarioRepository sistemaUsuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public ResponseEntity<?> login(LoginRequestDto request) {
        Optional<SistemaUsuarioEntity> userOpt = sistemaUsuarioRepository.findByUsernameAndEstatus(request.getUsername(), 1);
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos");
        }

        SistemaUsuarioEntity user = userOpt.get();
        String token = jwtService.generateToken(user.getUsername());
        return ResponseEntity.ok(new LoginResponseDto(token, user.getUsername(), user.getNombre()));
    }

    @Transactional
    public ResponseEntity<?> cambiarPassword(String username, CambiarPasswordDto request) {
        Optional<SistemaUsuarioEntity> userOpt = sistemaUsuarioRepository.findByUsernameAndEstatus(username, 1);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        SistemaUsuarioEntity user = userOpt.get();
        if (!passwordEncoder.matches(request.getPasswordActual(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La contraseña actual no es correcta");
        }
        if (request.getPasswordNueva() == null || request.getPasswordNueva().length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La nueva contraseña debe tener al menos 8 caracteres");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPasswordNueva()));
        user.setDateUpdate(LocalDateTime.now());
        sistemaUsuarioRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
