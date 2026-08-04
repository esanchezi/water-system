package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.PreregistroUsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPreregistroUsuarioRepository extends JpaRepository<PreregistroUsuarioEntity, Integer> {

    // Preregistros de una casa en particular (pantalla de detalle de casa).
    List<PreregistroUsuarioEntity> findByWaterHouse_CasaIdOrderByFechaRegistroDesc(Integer casaId);

    // Todos los pendientes, de todas las casas -- útil para un reporte
    // general de "gente por evaluar" más adelante.
    List<PreregistroUsuarioEntity> findByEstatusOrderByFechaRegistroAsc(Integer estatus);

}
