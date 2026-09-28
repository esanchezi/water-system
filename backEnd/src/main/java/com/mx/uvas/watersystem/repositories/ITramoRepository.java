package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.TramoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ITramoRepository extends JpaRepository<TramoEntity, Integer> {
    List<TramoEntity> findAllByEstatusOrderByNombreAsc(Integer estatus);
}
