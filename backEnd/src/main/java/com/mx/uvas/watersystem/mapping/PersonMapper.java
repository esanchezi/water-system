package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.*;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {

    public PersonDto entityToDto(PersonEntity entity) {
        var response = new PersonDto();
        BeanUtils.copyProperties(entity,response);
        return response;
    }
}
