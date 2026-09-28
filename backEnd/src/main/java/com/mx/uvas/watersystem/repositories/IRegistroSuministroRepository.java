package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.dto.DiasPorPozoDto;
import com.mx.uvas.watersystem.dto.DiasPorTramoDto;
import com.mx.uvas.watersystem.model.RegistroSuministroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IRegistroSuministroRepository extends JpaRepository<RegistroSuministroEntity, Integer> {

    List<RegistroSuministroEntity> findAllByEstatusOrderByFechaDesc(Integer estatus);

    // Cuántos días (registros) tiene cada pozo -- responde "cuántos días
    // tienen agua y de qué pozo".
    @Query("SELECT new com.mx.uvas.watersystem.dto.DiasPorPozoDto(" +
            "p.pozoId, p.nombre, COUNT(DISTINCT r.registroId)) " +
            "FROM RegistroSuministroEntity r JOIN r.pozo p " +
            "WHERE r.estatus = 1 " +
            "GROUP BY p.pozoId, p.nombre " +
            "ORDER BY p.nombre")
    List<DiasPorPozoDto> getDiasPorPozo();

    // Cuántos días ha recibido agua cada tramo (contando cada registro en
    // el que aparece, sin importar el turno).
    @Query("SELECT new com.mx.uvas.watersystem.dto.DiasPorTramoDto(" +
            "t.tramoId, t.nombre, COUNT(DISTINCT r.registroId)) " +
            "FROM RegistroSuministroTramoEntity rst " +
            "JOIN rst.tramo t " +
            "JOIN rst.registro r " +
            "WHERE r.estatus = 1 " +
            "GROUP BY t.tramoId, t.nombre " +
            "ORDER BY t.nombre")
    List<DiasPorTramoDto> getDiasPorTramo();
}
