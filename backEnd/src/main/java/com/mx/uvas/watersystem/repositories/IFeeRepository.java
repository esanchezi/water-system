package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.FeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IFeeRepository extends JpaRepository <FeeEntity,Integer>{
}
