package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.ResetPasswordDto;
import com.mx.uvas.watersystem.dto.SistemaUsuarioCreateDto;
import com.mx.uvas.watersystem.dto.SistemaUsuarioDto;
import com.mx.uvas.watersystem.model.SistemaUsuarioEntity;
import com.mx.uvas.watersystem.repositories.ISistemaUsuarioRepository;
import com.mx.uvas.watersystem.response.SistemaUsuarioRestResponse;
import com.mx.uvas.watersystem.services.ISistemaUsuarioService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Administra las cuentas de acceso (login) -- NO confundir con
// WaterUserService (usuarios del servicio de agua). Pensado para que la
// propia administradora pueda dar de alta cuentas con rol restringido
// (ej. "USUARIO1") sin depender de pedirlo cada vez.
@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class SistemaUsuarioService implements ISistemaUsuarioService {

    private final ISistemaUsuarioRepository sistemaUsuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<SistemaUsuarioRestResponse> findAll() {
        SistemaUsuarioRestResponse response = new SistemaUsuarioRestResponse();
        try {
            List<SistemaUsuarioEntity> entities = sistemaUsuarioRepository.findAllByEstatusOrderByUsernameAsc(1);
            response.setData(entities.stream().map(this::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Cuentas encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar las cuentas", e);
        }
    }

    @Override
    public ResponseEntity<SistemaUsuarioRestResponse> create(SistemaUsuarioCreateDto dto) {
        SistemaUsuarioRestResponse response = new SistemaUsuarioRestResponse();
        try {
            if (dto.getUsername() == null || dto.getUsername().isBlank()) {
                return ResponseHandler.handleBadRequest(response, "El usuario es obligatorio");
            }
            if (dto.getPassword() == null || dto.getPassword().length() < 8) {
                return ResponseHandler.handleBadRequest(response, "La contraseña debe tener al menos 8 caracteres");
            }
            if (dto.getRol() == null || dto.getRol().isBlank()) {
                return ResponseHandler.handleBadRequest(response, "Elige un rol para la cuenta");
            }
            Optional<SistemaUsuarioEntity> existente = sistemaUsuarioRepository.findByUsername(dto.getUsername());
            if (existente.isPresent()) {
                return ResponseHandler.handleBadRequest(response, "Ya existe una cuenta con ese usuario");
            }

            SistemaUsuarioEntity entity = SistemaUsuarioEntity.builder()
                    .username(dto.getUsername().trim())
                    .passwordHash(passwordEncoder.encode(dto.getPassword()))
                    .nombre(dto.getNombre())
                    .rol(dto.getRol().trim().toUpperCase())
                    .estatus(1)
                    .dateAdd(LocalDateTime.now())
                    .build();
            SistemaUsuarioEntity saved = sistemaUsuarioRepository.save(entity);
            response.setData(List.of(entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Cuenta creada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al crear la cuenta", e);
        }
    }

    @Override
    public ResponseEntity<SistemaUsuarioRestResponse> update(Integer id, SistemaUsuarioDto dto) {
        SistemaUsuarioRestResponse response = new SistemaUsuarioRestResponse();
        try {
            Optional<SistemaUsuarioEntity> optional = sistemaUsuarioRepository.findById(id);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Cuenta no encontrada con id: " + id);
            }
            if (dto.getRol() == null || dto.getRol().isBlank()) {
                return ResponseHandler.handleBadRequest(response, "Elige un rol para la cuenta");
            }
            SistemaUsuarioEntity entity = optional.get();
            entity.setNombre(dto.getNombre());
            entity.setRol(dto.getRol().trim().toUpperCase());
            entity.setDateUpdate(LocalDateTime.now());
            SistemaUsuarioEntity saved = sistemaUsuarioRepository.save(entity);
            response.setData(List.of(entityToDto(saved)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Cuenta actualizada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al actualizar la cuenta", e);
        }
    }

    @Override
    public ResponseEntity<SistemaUsuarioRestResponse> resetPassword(Integer id, ResetPasswordDto dto) {
        SistemaUsuarioRestResponse response = new SistemaUsuarioRestResponse();
        try {
            Optional<SistemaUsuarioEntity> optional = sistemaUsuarioRepository.findById(id);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Cuenta no encontrada con id: " + id);
            }
            if (dto.getPasswordNueva() == null || dto.getPasswordNueva().length() < 8) {
                return ResponseHandler.handleBadRequest(response, "La contraseña debe tener al menos 8 caracteres");
            }
            SistemaUsuarioEntity entity = optional.get();
            entity.setPasswordHash(passwordEncoder.encode(dto.getPasswordNueva()));
            entity.setDateUpdate(LocalDateTime.now());
            sistemaUsuarioRepository.save(entity);
            response.setData(List.of(entityToDto(entity)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Contraseña restablecida correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al restablecer la contraseña", e);
        }
    }

    @Override
    public ResponseEntity<SistemaUsuarioRestResponse> deactivate(Integer id) {
        SistemaUsuarioRestResponse response = new SistemaUsuarioRestResponse();
        try {
            Optional<SistemaUsuarioEntity> optional = sistemaUsuarioRepository.findById(id);
            if (optional.isEmpty()) {
                return ResponseHandler.handleNotFoundException(response, "Cuenta no encontrada con id: " + id);
            }
            SistemaUsuarioEntity entity = optional.get();
            entity.setEstatus(0);
            entity.setDateUpdate(LocalDateTime.now());
            sistemaUsuarioRepository.save(entity);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Cuenta desactivada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al desactivar la cuenta", e);
        }
    }

    private SistemaUsuarioDto entityToDto(SistemaUsuarioEntity entity) {
        SistemaUsuarioDto dto = new SistemaUsuarioDto();
        dto.setSistemaUsuarioId(entity.getSistemaUsuarioId());
        dto.setUsername(entity.getUsername());
        dto.setNombre(entity.getNombre());
        dto.setRol(entity.getRol());
        dto.setEstatus(entity.getEstatus());
        return dto;
    }
}
