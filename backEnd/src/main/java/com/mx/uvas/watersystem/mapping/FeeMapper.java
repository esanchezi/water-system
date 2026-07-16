package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.*;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.FeeAmountEntity;
import com.mx.uvas.watersystem.model.FeeEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class FeeMapper {

    public FeeDto entityToDto(FeeEntity entity) {
        var response = new FeeDto();
        BeanUtils.copyProperties(entity,response);
        for (FeeAmountEntity amountEntity : entity.getFeeAmount()) {
            var amount = new FeeAmountDto();
            BeanUtils.copyProperties(amountEntity,amount);
            response.getAmount().add(amount);
        }
        if (entity.getUso() != null) {
            var uso = new CatalogOptionsDto();
            BeanUtils.copyProperties(entity.getUso(), uso);
            response.setUso(uso);
        }

        if (entity.getUserType() != null) {
            var userType = new CatalogOptionsDto();
            BeanUtils.copyProperties(entity.getUserType(), userType);
            response.setUserType(userType);
        }

        return response;
    }

    public FeeEntity dtoToEntity(FeeDto dto, CatalogOptionsEntity uso, CatalogOptionsEntity userType) {
        return FeeEntity.builder()
                .observaciones(dto.getObservaciones())
                .uso(uso)
                .userType(userType)
                .build();
    }

    public FeeAmountEntity dtoToAmountEntity(FeeAmountDto dto, FeeEntity fee) {
        return FeeAmountEntity.builder()
                .cuota(dto.getCuota())
                .vigencia(dto.getVigencia())
                .observaciones(dto.getObservaciones())
                .fee(fee)
                .build();
    }
}
