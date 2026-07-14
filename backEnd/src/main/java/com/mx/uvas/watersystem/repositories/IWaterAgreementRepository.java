package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterAgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IWaterAgreementRepository extends JpaRepository<WaterAgreementEntity, Integer> {

    @Query("SELECT wa FROM WaterAgreementEntity wa WHERE wa.estatus = 1 ORDER BY wa.fecha DESC")
    List<WaterAgreementEntity> findAllActivos();

    @Query("SELECT wa " +
            "FROM WaterAgreementEntity wa " +
            "JOIN wa.waterUser wu " +
            "WHERE wa.estatus = 1 AND wu.noUsuario = :noUser " +
            "ORDER BY wa.fecha DESC")
    List<WaterAgreementEntity> findByNoUser(Integer noUser);

}
