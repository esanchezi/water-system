package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.FeeAmountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IFeeAmountRepository extends JpaRepository <FeeAmountEntity,Integer>{
    Optional<FeeAmountEntity> findByFee_CuotaIdAndVigencia(Integer cuotaId, Integer vigencia);

    // Todos los montos vigentes de un año en una sola consulta — usado por
    // reportes que recorren todos los usuarios (evita una query por usuario).
    List<FeeAmountEntity> findByVigencia(Integer vigencia);
}
