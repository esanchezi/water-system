package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.WaterUserCensusDto;
import com.mx.uvas.watersystem.response.WaterUserCensusResumenRestResponse;
import com.mx.uvas.watersystem.response.WaterUserCensusRestResponse;
import org.springframework.http.ResponseEntity;

public interface IWaterUserCensusService {

    // Integrantes activos de un usuario (ficha de usuario).
    ResponseEntity<WaterUserCensusRestResponse> findByAguaUsuarioId(Integer aguaUsuarioId);

    // Agrega una persona al censo de un usuario. La edad es opcional.
    ResponseEntity<WaterUserCensusRestResponse> create(Integer aguaUsuarioId, WaterUserCensusDto dto);

    // Edita edad/observaciones de un integrante -- al cambiar la edad,
    // reinicia el año de registro al actual para que el cálculo de edad
    // futura siga siendo correcto.
    ResponseEntity<WaterUserCensusRestResponse> update(Integer censoId, WaterUserCensusDto dto);

    // Baja lógica (ej. la persona ya no vive ahí), nunca borra el registro.
    ResponseEntity<WaterUserCensusRestResponse> deactivate(Integer censoId);

    // Reporte agregado: total de personas por rango de edad + "sin
    // clasificar" (integrantes sin edad capturada), de todos los usuarios.
    ResponseEntity<WaterUserCensusResumenRestResponse> resumenPorEdad();
}
