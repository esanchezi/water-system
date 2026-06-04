package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.AdressDto;
import com.mx.uvas.watersystem.dto.PersonDto;
import com.mx.uvas.watersystem.dto.WaterUserDto;
import com.mx.uvas.watersystem.helpers.AdressHelper;
import com.mx.uvas.watersystem.helpers.PersonHelper;
import com.mx.uvas.watersystem.mapping.AdressMapper;
import com.mx.uvas.watersystem.mapping.PersonMapper;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.FeeAmountEntity;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IPersonRepository;
import com.mx.uvas.watersystem.response.PersonRestResponse;
import com.mx.uvas.watersystem.response.WaterUserRestResponse;
import com.mx.uvas.watersystem.services.IPersonService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Transactional
@Service
@Slf4j
@AllArgsConstructor
public class PersonService implements IPersonService {

    private final IPersonRepository personRepository;
    private final PersonMapper personMapper;
    private final AdressMapper adressMapper;
    private final PersonHelper personHelper;
    private final AdressHelper adressHelper;

    private static final String USUARIOS_FOUND_MESSAGE = "Usuarios encontrados";
    private static final String USUARIOS_NOT_FOUND_MESSAGE = "Usuarios no encontrados";
    private static final String ERROR_SEARCHING_USUARIOS_MESSAGE = "Error al consultar lista de Usuarios";


    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PersonRestResponse> search() {
        return handleFindAll(personRepository.findAll());
    }

    @Override
    public ResponseEntity<PersonRestResponse> findByIdPersona(Integer personaId) {
        return handleFindSingle(personRepository.findByPersonaId(personaId).orElse(null));
    }

    private ResponseEntity<PersonRestResponse> handleFindAll(List<PersonEntity> peopleList) {
        PersonRestResponse response = new PersonRestResponse();
        try {

            List<PersonDto> peopleListDtos = peopleList.isEmpty()
                    ? Collections.emptyList()
                    : peopleList.stream()
                    .sorted(Comparator.comparing(PersonEntity::getApp))
                    .map(personMapper::entityToDto)
                    .toList();

            response.setData(peopleListDtos);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, USUARIOS_FOUND_MESSAGE);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, USUARIOS_NOT_FOUND_MESSAGE);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, ERROR_SEARCHING_USUARIOS_MESSAGE, e);
        }
    }

    @Override
    @Transactional
    public PersonDto create(PersonDto request) {
        return personMapper.entityToDto(personHelper.saveOrUpdatePerson(request));
    }

    @Override
    @Transactional
    public WaterUserDto savePersonAndAddress(WaterUserDto request) {
        PersonDto updatedPerson = personMapper.entityToDto(
                personHelper.saveOrUpdatePerson(request.getPerson())
        );
        request.setPerson(updatedPerson);

        AdressDto updatedAdress = adressMapper.entityToDto(
                adressHelper.saveOrUpdateAdress(request.getAdress())
        );
        request.setAdress(updatedAdress);

        return request;
    }


    private ResponseEntity<PersonRestResponse> handleFindSingle(PersonEntity person) {
        PersonRestResponse response = new PersonRestResponse();
        try {
            if (person == null) {
                throw new NoSuchElementException("Persona no encontrada");
            }
            List<PersonDto> waterUserDtos = Collections.singletonList(personMapper.entityToDto(person));
            response.setData(waterUserDtos);
            response.addMetadata("Respuesta ok", "00", "Persona encontrada");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseHandler.handleNotFoundException(response, "Persona no encontrada");
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar Persona", e);
        }
    }
}
