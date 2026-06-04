package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.model.WaterUserNoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IWaterUserNoticeRepository extends JpaRepository<WaterUserNoticeEntity,Integer>{

    @Query("SELECT wun " +
            "FROM  WaterUserNoticeEntity wun " +
            "JOIN  wun.waterUser wu " +
            "WHERE wu.estatus = 1 AND wu.noUsuario=:noUser " +
            "ORDER BY wu.noUsuario")
    List<WaterUserNoticeEntity> findByNoUser(Integer noUser);

}
