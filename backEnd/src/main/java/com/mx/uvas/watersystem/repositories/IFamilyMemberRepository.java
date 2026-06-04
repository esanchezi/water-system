package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.FamilyMemberEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IFamilyMemberRepository extends JpaRepository<FamilyMemberEntity,Integer>{

    @Query("SELECT fi2 " +
            "FROM FamilyMemberEntity fi1 " +
            "JOIN FamilyMemberEntity fi2 ON fi1.familia = fi2.familia " +
            "WHERE fi1.person.personaId = :personId " +
            "AND fi2.person.personaId != :personId" )
    List<FamilyMemberEntity> findByIdPerson(Integer personId);

}
