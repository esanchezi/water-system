package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.AdressDto;
import com.mx.uvas.watersystem.dto.PersonDto;
import com.mx.uvas.watersystem.model.AdressEntity;
import com.mx.uvas.watersystem.model.PersonEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class AdressMapper {
    public AdressDto entityToDto(AdressEntity entity) {
        if (entity == null) return null;

        AdressDto dto = new AdressDto();
        dto.setDireccionId(entity.getDireccionId());
        dto.setCalle(entity.getCalle());
        dto.setNumero(entity.getNumero());
        dto.setReferencia(entity.getReferencia());
        dto.setEntrecalle1(entity.getEntrecalle1());
        dto.setSeccionId(entity.getCatSeccion() != null ? entity.getCatSeccion().getCatalogoOpcionesId() : null);
        return dto;
    }

    public AdressEntity dtoToEntity(AdressDto dto) {
        if (dto == null) return null;

        AdressEntity entity = new AdressEntity();
        entity.setDireccionId(dto.getDireccionId());
        entity.setCalle(dto.getCalle());
        entity.setNumero(dto.getNumero());
        entity.setReferencia(dto.getReferencia());
        entity.setEntrecalle1(dto.getEntrecalle1());

        return entity;
    }
}
