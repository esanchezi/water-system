package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterEgresoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IWaterEgresoRepository extends JpaRepository<WaterEgresoEntity, Integer> {

    // Egresos activos de nivel 1 (columna nivel = 1, no detalle de otro
    // vale), usados para el reporte "Totales por año" y comparar contra lo
    // recaudado en efectivo.
    @Query("SELECT e FROM WaterEgresoEntity e " +
            "WHERE e.estatus = 1 " +
            "AND e.nivel = 1")
    List<WaterEgresoEntity> findNivel1Activos();
}
