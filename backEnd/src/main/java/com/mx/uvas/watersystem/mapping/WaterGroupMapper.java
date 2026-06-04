package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.PersonDto;
import com.mx.uvas.watersystem.dto.WaterGroupDto;
import com.mx.uvas.watersystem.dto.WaterUserDto;
import com.mx.uvas.watersystem.model.WaterGroupEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class WaterGroupMapper {
    public WaterGroupDto entityToDto(WaterGroupEntity entity) {
        var response = new WaterGroupDto();
        BeanUtils.copyProperties(entity,response);

        if (entity.getListWaterUser() != null) {
            var waterUserDtos = entity.getListWaterUser()
                    .stream()
                    .map(this::waterUserEntityToDto)
                    .toList(); // Java 16+ tiene .toList() directo
            response.setListWaterUser(waterUserDtos);
        }
        return response;
    }

    public WaterUserDto waterUserEntityToDto(WaterUserEntity entity) {
        var response = new WaterUserDto();
        BeanUtils.copyProperties(entity,response);
        if (entity.getPerson() != null){
            var person = new PersonDto();
            BeanUtils.copyProperties(entity.getPerson(),person);
            response.setPerson(person);
        }else{
            response.setPerson(new PersonDto());
        }

        return response;
    }

    public WaterGroupEntity dtoToEntity(WaterGroupDto dto) {
        return WaterGroupEntity.builder()
                .nombre(dto.getNombre())
                .observaciones(dto.getObservaciones())
                .build();
    }
}
