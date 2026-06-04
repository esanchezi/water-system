package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterExpensesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IWaterExpensesRepository extends JpaRepository <WaterExpensesEntity,Integer>{
}
