package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterUserRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IWaterUserRevisionRepository extends JpaRepository<WaterUserRevisionEntity, Integer> {

    List<WaterUserRevisionEntity> findByWaterUser_AguaUsuarioIdAndEstatusOrderByFechaDesc(Integer aguaUsuarioId, Integer estatus);
}
