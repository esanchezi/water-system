package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AssemblyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IAssemblyRepository extends JpaRepository<AssemblyEntity,Integer>{

    @Query("SELECT a " +
            "FROM AssemblyEntity a " +
            "ORDER BY a.date")
    List<AssemblyEntity> findAll(Integer noUser);

    @Query("SELECT a " +
            "FROM AssemblyEntity a " +
            "JOIN a.assemblyAttendanceEntity aa " +
            "WHERE aa.waterUser.noUsuario = :noUser " +
            "ORDER BY a.date")
    List<AssemblyEntity> findByNoUsuario(Integer noUser);

}
