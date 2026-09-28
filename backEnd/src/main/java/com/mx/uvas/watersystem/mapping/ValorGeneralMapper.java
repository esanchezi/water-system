package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.ValorGeneralDto;
import com.mx.uvas.watersystem.model.ValorGeneralEntity;
import org.springframework.stereotype.Component;

@Component
public class ValorGeneralMapper {

    public ValorGeneralDto entityToDto(ValorGeneralEntity entity) {
        ValorGeneralDto dto = new ValorGeneralDto();
        dto.setValorGeneralId(entity.getValorGeneralId());
        dto.setClave(entity.getClave());
        dto.setNombre(entity.getNombre());
        dto.setVigencia(entity.getVigencia());
        dto.setMonto(entity.getMonto());
        dto.setObservaciones(entity.getObservaciones());
        dto.setActivo(entity.getEstatus() != null && entity.getEstatus() == 1);
        return dto;
    }

    public ValorGeneralEntity dtoToEntity(ValorGeneralDto dto) {
        return ValorGeneralEntity.builder()
                .clave(dto.getClave())
                .nombre(dto.getNombre())
                .vigencia(dto.getVigencia())
                .monto(dto.getMonto())
                .observaciones(dto.getObservaciones())
                .estatus(1)
                .build();
    }
}
