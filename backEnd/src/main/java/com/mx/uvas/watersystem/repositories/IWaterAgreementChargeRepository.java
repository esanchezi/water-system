package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterAgreementChargeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IWaterAgreementChargeRepository extends JpaRepository<WaterAgreementChargeEntity, Integer> {
}
