package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.*;
import com.mx.uvas.watersystem.model.WaterReceiptHistoryEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class WaterReceiptHistoryMapper {

    public WaterReceiptHistoryDto entityToDto(WaterReceiptHistoryEntity entity) {
        var response = new WaterReceiptHistoryDto();
        BeanUtils.copyProperties(entity,response);
        var user = new WaterUserDto();
        if(entity.getWaterUser() != null){
            BeanUtils.copyProperties(entity.getWaterUser(),user);
            response.setWaterUser(user);
            if (entity.getWaterUser().getPerson() != null){
                var person = new PersonDto();
                BeanUtils.copyProperties(entity.getWaterUser().getPerson(),person);
                response.getWaterUser().setPerson(person);
            }else{
                response.getWaterUser().setPerson(new PersonDto());
            }
        }else{
            response.setWaterUser(new WaterUserDto());
        }

        if(entity.getComite() != null){
            var comite = new CatalogOptionsDto();
            BeanUtils.copyProperties(entity.getComite(),comite);
            response.setComite(comite);
        }

        return response;
    }
}
