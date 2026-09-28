package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.PozoDto;
import com.mx.uvas.watersystem.model.PozoEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class PozoMapper {
    public PozoDto entityToDto(PozoEntity entity) {
        var dto = new PozoDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public PozoEntity dtoToEntity(PozoDto dto) {
        return PozoEntity.builder()
                .nombre(dto.getNombre())
                .lat(dto.getLat())
                .lng(dto.getLng())
                .observaciones(dto.getObservaciones())
                .build();
    }
}
