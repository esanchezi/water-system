package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPersonRepository extends JpaRepository <PersonEntity,Integer>{

    Optional<PersonEntity> findByPersonaId(Integer personaId);
}
