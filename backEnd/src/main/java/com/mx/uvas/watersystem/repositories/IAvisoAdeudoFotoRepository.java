package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AvisoAdeudoFotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAvisoAdeudoFotoRepository extends JpaRepository<AvisoAdeudoFotoEntity, Integer> {

    List<AvisoAdeudoFotoEntity> findByAvisoAdeudo_AvisoAdeudoIdAndEstatus(Integer avisoAdeudoId, Integer estatus);
}
