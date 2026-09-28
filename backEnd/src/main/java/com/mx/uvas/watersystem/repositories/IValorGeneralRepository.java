package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.ValorGeneralEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IValorGeneralRepository extends JpaRepository<ValorGeneralEntity, Integer> {

    List<ValorGeneralEntity> findByEstatusOrderByClaveAscVigenciaDesc(Integer estatus);

    Optional<ValorGeneralEntity> findByClaveAndVigenciaAndEstatus(String clave, Integer vigencia, Integer estatus);

    // Para resolver el "monto vigente" de una clave cuando no hay un
    // renglón exacto para el año consultado -- se toma el más reciente
    // anterior o igual (ver ValorGeneralService.getMontoVigente).
    List<ValorGeneralEntity> findByClaveAndEstatusOrderByVigenciaDesc(String clave, Integer estatus);
}
