package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AvisoBombaFotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAvisoBombaFotoRepository extends JpaRepository<AvisoBombaFotoEntity, Integer> {

    List<AvisoBombaFotoEntity> findByAvisoBomba_AvisoBombaIdAndEstatus(Integer avisoBombaId, Integer estatus);
}
