package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.ValorGeneralDto;
import com.mx.uvas.watersystem.response.ValorGeneralRestResponse;
import org.springframework.http.ResponseEntity;

public interface IValorGeneralService {

    ResponseEntity<ValorGeneralRestResponse> findAll();

    ResponseEntity<ValorGeneralRestResponse> create(ValorGeneralDto dto);

    ResponseEntity<ValorGeneralRestResponse> update(Integer valorGeneralId, ValorGeneralDto dto);

    ResponseEntity<ValorGeneralRestResponse> deactivate(Integer valorGeneralId);

    // Monto vigente de una clave para un año dado -- toma el renglón exacto
    // de ese año, o si no existe, el más reciente de un año anterior (para
    // no dejar el cálculo en $0.00 solo porque todavía no se capturó el
    // valor del año en curso). 0.0 si no hay ningún renglón capturado.
    Double getMontoVigente(String clave, Integer anio);
}
