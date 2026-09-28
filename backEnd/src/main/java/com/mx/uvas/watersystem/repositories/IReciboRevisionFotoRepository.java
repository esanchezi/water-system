package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.ReciboRevisionFotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IReciboRevisionFotoRepository extends JpaRepository<ReciboRevisionFotoEntity, Integer> {

    List<ReciboRevisionFotoEntity> findByEstatusOrderByDateAddDesc(Integer estatus);
}
