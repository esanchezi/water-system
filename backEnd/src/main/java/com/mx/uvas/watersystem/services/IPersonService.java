package com.mx.uvas.watersystem.services;

import com.mx.uvas.watersystem.dto.PersonDto;
import com.mx.uvas.watersystem.dto.WaterUserDto;
import com.mx.uvas.watersystem.response.PersonRestResponse;
import com.mx.uvas.watersystem.response.WaterUserRestResponse;
import org.springframework.http.ResponseEntity;

public interface IPersonService {
    ResponseEntity<PersonRestResponse> search();

    ResponseEntity<PersonRestResponse> findByIdPersona(Integer personaId);

    PersonDto create(PersonDto request);

    WaterUserDto savePersonAndAddress(WaterUserDto request);
}
