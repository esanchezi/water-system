package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.PreregistroUsuarioDto;
import com.mx.uvas.watersystem.response.PreregistroUsuarioRestResponse;
import org.springframework.http.ResponseEntity;

public interface IPreregistroUsuarioService {

    ResponseEntity<PreregistroUsuarioRestResponse> findByCasaId(Integer casaId);

    ResponseEntity<PreregistroUsuarioRestResponse> create(Integer casaId, PreregistroUsuarioDto dto);

    ResponseEntity<PreregistroUsuarioRestResponse> update(Integer preregistroId, PreregistroUsuarioDto dto);

    // Marca el registro como Convertido y guarda a qué usuario real
    // corresponde. La creación del usuario en sí se hace por separado con
    // el flujo normal de "Nuevo usuario".
    ResponseEntity<PreregistroUsuarioRestResponse> marcarConvertido(Integer preregistroId, Integer aguaUsuarioId);

    // Descartado = ya no se va a convertir en usuario (con motivo opcional
    // de por qué).
    ResponseEntity<PreregistroUsuarioRestResponse> marcarDescartado(Integer preregistroId, String motivo);
}
