package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterUserAnnualPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IWaterUserAnnualPaymentRepository extends JpaRepository <WaterUserAnnualPaymentEntity,Integer>{

    boolean existsByWaterUser_AguaUsuarioIdAndAnio(Integer aguaUsuarioId, Integer anio);


}