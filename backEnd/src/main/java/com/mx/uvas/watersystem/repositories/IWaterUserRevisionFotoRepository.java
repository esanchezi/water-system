package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterUserRevisionFotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IWaterUserRevisionFotoRepository extends JpaRepository<WaterUserRevisionFotoEntity, Integer> {

    List<WaterUserRevisionFotoEntity> findByRevision_RevisionIdAndEstatus(Integer revisionId, Integer estatus);
}
