package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AssemblyAttendanceEntity;
import com.mx.uvas.watersystem.model.AssemblyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IAssemblyAttendanceRepository extends JpaRepository<AssemblyAttendanceEntity,Integer>{
    @Query("SELECT aa " +
            "FROM AssemblyAttendanceEntity aa " +
            "JOIN  aa.assemblyEntity a " +
            "WHERE a.asambleaId = :idAssembly ")
    List<AssemblyAttendanceEntity> findByIdAssembly(Integer idAssembly);

}
