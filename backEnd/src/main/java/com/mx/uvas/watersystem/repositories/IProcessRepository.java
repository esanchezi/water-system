package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.FamilyMemberEntity;
import com.mx.uvas.watersystem.model.ProcessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IProcessRepository extends JpaRepository<ProcessEntity,Integer>{

    @Query("SELECT DISTINCT t FROM ProcessEntity t " +
            "JOIN t.person p " +
            "JOIN FamilyMemberEntity fme ON fme.person = p " +
            "JOIN FamilyEntity fe ON fme.familia = fe " +
            "JOIN HouseFamilyEntity cf ON cf.familia = fe " +
            "JOIN HouseEntity c ON cf.casa = c " +
            "WHERE c.id = (" +
            "    SELECT cf2.casa.id FROM HouseFamilyEntity  cf2 " +
            "    JOIN FamilyEntity fe2 ON cf2.familia = fe2 " +
            "    JOIN FamilyMemberEntity fme2 ON fe2 = fme2.familia " +
            "    WHERE fme2.person.personaId = :personId" +
            ") " +
            "AND p.personaId != :personId")
    List<ProcessEntity> findByIdPerson(Integer personId);

}
