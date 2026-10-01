package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AvisoInformativoAdeudoFotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAvisoInformativoAdeudoFotoRepository extends JpaRepository<AvisoInformativoAdeudoFotoEntity, Integer> {

    List<AvisoInformativoAdeudoFotoEntity> findByAvisoInformativoAdeudo_AvisoInformativoAdeudoIdAndEstatus(Integer avisoInformativoAdeudoId, Integer estatus);
}
