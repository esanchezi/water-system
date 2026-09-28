package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.ResetPasswordDto;
import com.mx.uvas.watersystem.dto.SistemaUsuarioCreateDto;
import com.mx.uvas.watersystem.dto.SistemaUsuarioDto;
import com.mx.uvas.watersystem.response.SistemaUsuarioRestResponse;
import org.springframework.http.ResponseEntity;

public interface ISistemaUsuarioService {

    ResponseEntity<SistemaUsuarioRestResponse> findAll();

    ResponseEntity<SistemaUsuarioRestResponse> create(SistemaUsuarioCreateDto dto);

    ResponseEntity<SistemaUsuarioRestResponse> update(Integer id, SistemaUsuarioDto dto);

    ResponseEntity<SistemaUsuarioRestResponse> resetPassword(Integer id, ResetPasswordDto dto);

    ResponseEntity<SistemaUsuarioRestResponse> deactivate(Integer id);
}
