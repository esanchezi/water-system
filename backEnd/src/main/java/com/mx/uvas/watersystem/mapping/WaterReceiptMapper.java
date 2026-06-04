package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.*;
import com.mx.uvas.watersystem.model.WaterReceiptEntity;
import com.mx.uvas.watersystem.model.WaterReceiptPaymentEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WaterReceiptMapper {

    public WaterReceiptDto entityToDto(WaterReceiptEntity entity) {
        var response = new WaterReceiptDto();
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

        if (entity.getWaterReceiptPayment() != null) {
            List<WaterReceiptPaymentDto> paymentDtos = entity.getWaterReceiptPayment()
                    .stream()
                    .map(this::paymentToDto)
                    .collect(Collectors.toList());

            response.setWaterReceiptPayment(paymentDtos);
        } else {
            response.setWaterReceiptPayment(Collections.emptyList());
        }

        return response;
    }

    public WaterReceiptPaymentDto paymentToDto(WaterReceiptPaymentEntity entity) {
        var dto = new WaterReceiptPaymentDto();
        BeanUtils.copyProperties(entity, dto);
        dto.setConcepto(entity.getCatConcepto() != null ? entity.getCatConcepto().getNombre() : null);
        System.out.println( dto.getConcepto());
        return dto;
    }
}
