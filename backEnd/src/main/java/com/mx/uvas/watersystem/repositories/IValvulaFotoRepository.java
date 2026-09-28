package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.ValvulaFotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IValvulaFotoRepository extends JpaRepository<ValvulaFotoEntity, Integer> {

    List<ValvulaFotoEntity> findByValvula_ValvulaIdAndEstatus(Integer valvulaId, Integer estatus);
}
