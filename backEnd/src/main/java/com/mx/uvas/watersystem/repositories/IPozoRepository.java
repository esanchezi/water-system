package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.PozoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPozoRepository extends JpaRepository<PozoEntity, Integer> {
    List<PozoEntity> findAllByEstatusOrderByNombreAsc(Integer estatus);
}
