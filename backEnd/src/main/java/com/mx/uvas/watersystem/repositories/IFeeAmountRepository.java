package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.FeeAmountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IFeeAmountRepository extends JpaRepository <FeeAmountEntity,Integer>{
    Optional<FeeAmountEntity> findByFee_CuotaIdAndVigencia(Integer cuotaId, Integer vigencia);
}
