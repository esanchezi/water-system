package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.*;
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
        var uso = new CatalogOptionsDto();
        BeanUtils.copyProperties(entity.getUso(),uso);
        response.setUso(uso);

        var userType = new CatalogOptionsDto();
        BeanUtils.copyProperties(entity.getUserType(),userType);
        response.setUserType(userType);

        return response;
    }
}
