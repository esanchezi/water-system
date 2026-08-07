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

    // Preregistros con deuda aproximada capturada (aportaciones y/o
    // multas/recargos > 0), de todas las casas -- para considerarlos junto
    // con los deudores reales aunque todavía no sean usuario formal.
    // Excluye los ya Convertidos (esos ya cuentan como deudor real).
    ResponseEntity<PreregistroUsuarioRestResponse> findConDeuda();

    // Listado global (todas las casas) para el módulo de preregistro.
    ResponseEntity<PreregistroUsuarioRestResponse> findAllConDetalle();

    // Preregistros ya vinculados a un grupo (ficha de grupo).
    ResponseEntity<PreregistroUsuarioRestResponse> findByGrupoId(Integer grupoId);

    // Asigna o quita (grupoId = null) el grupo al que probablemente se va a
    // unir esta persona cuando se convierta en usuario formal.
    ResponseEntity<PreregistroUsuarioRestResponse> asignarGrupo(Integer preregistroId, Integer grupoId);
}
