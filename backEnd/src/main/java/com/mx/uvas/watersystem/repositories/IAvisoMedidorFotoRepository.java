package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AvisoMedidorFotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAvisoMedidorFotoRepository extends JpaRepository<AvisoMedidorFotoEntity, Integer> {

    List<AvisoMedidorFotoEntity> findByAvisoMedidor_AvisoMedidorIdAndEstatus(Integer avisoMedidorId, Integer estatus);
}
