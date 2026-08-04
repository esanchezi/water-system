package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterUserCensusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IWaterUserCensusRepository extends JpaRepository<WaterUserCensusEntity, Integer> {

    // Integrantes activos de un usuario en particular (ficha de usuario).
    List<WaterUserCensusEntity> findByWaterUser_AguaUsuarioIdAndEstatus(Integer aguaUsuarioId, Integer estatus);

    // Todos los integrantes activos de todos los usuarios, para el reporte
    // agregado por edades.
    @Query("SELECT c FROM WaterUserCensusEntity c WHERE c.estatus = :estatus")
    List<WaterUserCensusEntity> findAllActivos(@Param("estatus") Integer estatus);

}
