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

    // Cabeceras de vale (nivel = 1) para el listado del módulo de egresos,
    // con sus líneas de categoría ya cargadas.
    @Query("SELECT DISTINCT e FROM WaterEgresoEntity e " +
            "LEFT JOIN FETCH e.lineas l " +
            "LEFT JOIN FETCH l.concepto " +
            "LEFT JOIN FETCH e.concepto " +
            "LEFT JOIN FETCH e.tipoComprobante " +
            "LEFT JOIN FETCH e.persona " +
            "WHERE e.estatus = 1 " +
            "AND e.nivel = 1 " +
            "ORDER BY e.fechaPago DESC")
    List<WaterEgresoEntity> findCabecerasActivas();

    @Query("SELECT e FROM WaterEgresoEntity e " +
            "LEFT JOIN FETCH e.lineas l " +
            "LEFT JOIN FETCH l.concepto " +
            "LEFT JOIN FETCH e.concepto " +
            "LEFT JOIN FETCH e.tipoComprobante " +
            "LEFT JOIN FETCH e.persona " +
            "WHERE e.aguaEgresosId = :id " +
            "AND e.nivel = 1")
    java.util.Optional<WaterEgresoEntity> findCabeceraById(Integer id);

    // Gastos sueltos ya capturados pero que todavía no se han juntado en un
    // vale (nivel = 2, padre_id = null). Base de la pantalla de "Emitir
    // vale" de fin de mes.
    @Query("SELECT e FROM WaterEgresoEntity e " +
            "LEFT JOIN FETCH e.concepto " +
            "LEFT JOIN FETCH e.persona " +
            "WHERE e.estatus = 1 " +
            "AND e.nivel = 2 " +
            "AND e.egresoPadre IS NULL " +
            "ORDER BY e.fechaPago")
    List<WaterEgresoEntity> findGastosPendientes();
}
