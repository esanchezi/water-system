package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterUserChargePaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IWaterUserChargePaymentRepository extends JpaRepository<WaterUserChargePaymentEntity, Integer> {
}
