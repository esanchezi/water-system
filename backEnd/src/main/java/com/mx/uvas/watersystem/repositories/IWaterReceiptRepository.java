package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IWaterReceiptRepository extends JpaRepository <WaterReceiptEntity,Integer>{

    WaterReceiptEntity findByNoFolio(Integer noFolio);

    @Query("SELECT wr " +
            "FROM WaterReceiptEntity wr " +
            "LEFT JOIN wr.waterUser u " +
            "WHERE wr.estatus = 1 AND " +
            "(wr.noFolio = :noFolio OR (u IS NOT NULL AND u.noUsuario = :noFolio)) " +
            "ORDER BY wr.noFolio")
    List<WaterReceiptEntity> findByNoFolioOrNoUsuario(Integer noFolio);

    @Query("SELECT wr " +
            "FROM WaterReceiptEntity wr " +
            "JOIN  wr.waterUser u " +
            "WHERE wr.estatus = 1 AND  u.noUsuario =:noUser " +
            "ORDER BY wr.fecha desc,wr.noFolio desc")
    List<WaterReceiptEntity> findByNoUsuario(Integer noUser);
    // Más reciente primero (fecha y luego folio) -- antes salía del más viejo
    // al más nuevo y los recientes quedaban hasta la última página.
    @Query("SELECT wr FROM WaterReceiptEntity wr WHERE wr.estatus = 1 ORDER BY wr.fecha DESC, wr.noFolio DESC")
    List<WaterReceiptEntity> findAllByEstatus();

    // Listado por rango de fechas (ambas inclusivas) -- para no traer todo el
    // historial de golpe a la pantalla de recibos. El servicio siempre manda
    // las dos fechas (si no vienen, usa un rango abierto).
    @Query("SELECT wr FROM WaterReceiptEntity wr " +
            "WHERE wr.estatus = 1 AND wr.fecha >= :desde AND wr.fecha <= :hasta " +
            "ORDER BY wr.fecha DESC, wr.noFolio DESC")
    List<WaterReceiptEntity> findByRangoFechas(java.time.LocalDate desde, java.time.LocalDate hasta);

}