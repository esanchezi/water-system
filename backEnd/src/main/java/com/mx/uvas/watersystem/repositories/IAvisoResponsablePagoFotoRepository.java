package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AvisoResponsablePagoFotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAvisoResponsablePagoFotoRepository extends JpaRepository<AvisoResponsablePagoFotoEntity, Integer> {

    List<AvisoResponsablePagoFotoEntity> findByResponsablePago_ResponsablePagoIdAndEstatus(Integer responsablePagoId, Integer estatus);
}
