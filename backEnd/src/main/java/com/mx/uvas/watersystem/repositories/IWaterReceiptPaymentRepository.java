package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterReceiptPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IWaterReceiptPaymentRepository extends JpaRepository <WaterReceiptPaymentEntity,Integer>{
}
