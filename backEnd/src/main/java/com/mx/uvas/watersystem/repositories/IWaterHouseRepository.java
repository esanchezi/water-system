package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterHouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IWaterHouseRepository extends JpaRepository <WaterHouseEntity,Integer>{

    @Query("SELECT h FROM WaterHouseEntity h " +
            "LEFT JOIN FETCH h.catCalle " +
            "ORDER BY h.catCalle.nombre ASC NULLS LAST, h.lado ASC NULLS LAST, h.casaNo ASC")
    List<WaterHouseEntity> findAllByOrderByCatCalle_NombreAscLadoAscCasaNoAsc();
}
