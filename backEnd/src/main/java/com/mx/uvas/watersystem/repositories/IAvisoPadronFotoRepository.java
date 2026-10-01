package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AvisoPadronFotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAvisoPadronFotoRepository extends JpaRepository<AvisoPadronFotoEntity, Integer> {

    List<AvisoPadronFotoEntity> findByAvisoPadron_AvisoPadronIdAndEstatus(Integer avisoPadronId, Integer estatus);
}
