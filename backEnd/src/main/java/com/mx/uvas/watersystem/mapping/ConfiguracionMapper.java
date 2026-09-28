package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.ConfiguracionDto;
import com.mx.uvas.watersystem.model.ConfiguracionEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ConfiguracionMapper {

    public ConfiguracionDto entityToDto(ConfiguracionEntity entity) {
        var dto = new ConfiguracionDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public ConfiguracionEntity dtoToEntity(ConfiguracionDto dto) {
        return ConfiguracionEntity.builder()
                .clave(dto.getClave())
                .valor(dto.getValor())
                .descripcion(dto.getDescripcion())
                .build();
    }
}
