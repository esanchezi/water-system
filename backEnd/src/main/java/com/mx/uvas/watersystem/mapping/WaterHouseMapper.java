package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.*;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class WaterHouseMapper {

    public WaterHouseDto entityToDto(WaterHouseEntity entity) {
        var response = new WaterHouseDto();
        BeanUtils.copyProperties(entity,response);

        if (entity.getListWaterUser() != null) {
            var waterUserDtos = entity.getListWaterUser()
                    .stream()
                    .map(this::waterUserEntityToDto)
                    .toList(); // Java 16+ tiene .toList() directo
            response.setListWaterUser(waterUserDtos);
        }

        if (entity.getCatSeccion() != null){
            response.setZonaId(entity.getCatSeccion().getCatalogoOpcionesId());
            response.setZona(entity.getCatSeccion().getNombre());
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

    public WaterHouseEntity dtoToEntity(WaterHouseDto dto, CatalogOptionsEntity zona) {
        return WaterHouseEntity.builder()
                .casaNo(dto.getCasaNo())
                .nombre(dto.getNombre())
                .catSeccion(zona)
                .lat(dto.getLat())
                .lng(dto.getLng())
                .observaciones(dto.getObservaciones())
                .build();
    }
}
