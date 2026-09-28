package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.CajaValvulaDto;
import com.mx.uvas.watersystem.dto.ValvulaDto;
import com.mx.uvas.watersystem.model.CajaValvulaEntity;
import com.mx.uvas.watersystem.model.ValvulaEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class CajaValvulaMapper {

    private final ValvulaMapper valvulaMapper;

    public CajaValvulaMapper(ValvulaMapper valvulaMapper) {
        this.valvulaMapper = valvulaMapper;
    }

    public CajaValvulaDto entityToDto(CajaValvulaEntity entity) {
        var dto = new CajaValvulaDto();
        BeanUtils.copyProperties(entity, dto);

        if (entity.getTramo() != null) {
            dto.setTramoId(entity.getTramo().getTramoId());
            dto.setTramoNombre(entity.getTramo().getNombre());
        }

        if (entity.getListValvula() != null) {
            dto.setListValvula(entity.getListValvula().stream()
                    .map(valvulaMapper::entityToDto)
                    .toList());
        }
        return dto;
    }

    public CajaValvulaEntity dtoToEntity(CajaValvulaDto dto) {
        var builder = CajaValvulaEntity.builder()
                .codigo(dto.getCodigo())
                .nombre(dto.getNombre())
                .lat(dto.getLat())
                .lng(dto.getLng())
                .observaciones(dto.getObservaciones())
                .lado(dto.getLado());

        var entity = builder.build();

        if (dto.getListValvula() != null) {
            var valvulas = dto.getListValvula().stream()
                    .map(vDto -> {
                        ValvulaEntity v = valvulaMapper.dtoToEntity(vDto);
                        v.setCaja(entity);
                        return v;
                    })
                    .collect(java.util.stream.Collectors.toSet());
            entity.setListValvula(valvulas);
        }
        return entity;
    }
}
