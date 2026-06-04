package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IWaterGroupRepository extends JpaRepository <WaterGroupEntity,Integer>{
    List<WaterGroupEntity> findAllByOrderByNombreAsc();
}
