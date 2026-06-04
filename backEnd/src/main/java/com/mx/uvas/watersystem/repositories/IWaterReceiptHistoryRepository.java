package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterReceiptHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IWaterReceiptHistoryRepository extends JpaRepository <WaterReceiptHistoryEntity,Integer>{

    @Query("SELECT wr " +
            "FROM WaterReceiptHistoryEntity wr " +
            "WHERE wr.estatus = 1 AND ( wr.noFolio LIKE %:noFolio% OR wr.nombre LIKE %:noFolio%) " +
            "ORDER BY wr.noFolio")
    List<WaterReceiptHistoryEntity> findByNoFolioOrNombre(String noFolio);
    @Query("SELECT wr FROM WaterReceiptHistoryEntity wr WHERE wr.estatus = 1 ORDER BY wr.noFolio")
    List<WaterReceiptHistoryEntity> findAllByEstatus();

}