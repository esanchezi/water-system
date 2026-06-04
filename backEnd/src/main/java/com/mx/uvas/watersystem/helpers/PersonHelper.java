package com.mx.uvas.watersystem.helpers;


import com.mx.uvas.watersystem.dto.PersonDto;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.repositories.IPersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Transactional
@Component
@AllArgsConstructor
public class PersonHelper {

    private final IPersonRepository personRepository;

    public PersonEntity createPerson(PersonDto personDto) {
        PersonEntity personEntity = buildNewPerson(personDto);
        return personRepository.save(personEntity);
    }

    public PersonEntity saveOrUpdatePerson(PersonDto personDto) {
        if (personDto.getPersonaId() != null && personRepository.existsById(personDto.getPersonaId())) {
            PersonEntity existing = personRepository.findById(personDto.getPersonaId())
                    .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + personDto.getPersonaId()));

            fillPersonData(existing, personDto);
            existing.setUserIdUpdate(1);
            existing.setDateUpdate(currentTimestamp());

            return personRepository.save(existing);
        } else {
            PersonEntity newPerson = buildNewPerson(personDto);
            return personRepository.save(newPerson);
        }
    }

    private PersonEntity buildNewPerson(PersonDto personDto) {
        PersonEntity entity = new PersonEntity();
        fillPersonData(entity, personDto);
        entity.setEstatus(1);
        entity.setUserIdAdd(1);
        entity.setDateAdd(currentTimestamp());
        return entity;
    }

    private void fillPersonData(PersonEntity entity, PersonDto dto) {
        entity.setNombre(dto.getNombre());
        entity.setNombre2(dto.getNombre2());
        entity.setApp(dto.getApp());
        entity.setApm(dto.getApm());
    }

    private LocalDateTime currentTimestamp() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}