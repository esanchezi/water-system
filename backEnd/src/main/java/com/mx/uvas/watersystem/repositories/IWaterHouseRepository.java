package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterHouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IWaterHouseRepository extends JpaRepository <WaterHouseEntity,Integer>{

    List<WaterHouseEntity> findAllByOrderByCatSeccion_NombreAscCasaNoAsc();
}
