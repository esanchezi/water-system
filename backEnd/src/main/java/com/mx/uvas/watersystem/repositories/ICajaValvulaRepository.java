package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.dto.ResumenValvulasDto;
import com.mx.uvas.watersystem.model.CajaValvulaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ICajaValvulaRepository extends JpaRepository<CajaValvulaEntity, Integer> {
    List<CajaValvulaEntity> findAllByEstatusOrderByNombreAsc(Integer estatus);

    @Query("SELECT new com.mx.uvas.watersystem.dto.ResumenValvulasDto(" +
            "COUNT(DISTINCT c.cajaId), COUNT(v.valvulaId)) " +
            "FROM CajaValvulaEntity c LEFT JOIN c.listValvula v " +
            "WHERE c.estatus = 1")
    ResumenValvulasDto getResumen();
}
