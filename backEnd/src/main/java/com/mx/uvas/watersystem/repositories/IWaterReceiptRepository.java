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
    @Query("SELECT wr FROM WaterReceiptEntity wr WHERE wr.estatus = 1 ORDER BY wr.noFolio")
    List<WaterReceiptEntity> findAllByEstatus();

}