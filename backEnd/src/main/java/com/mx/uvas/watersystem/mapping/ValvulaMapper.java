package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.ValvulaDto;
import com.mx.uvas.watersystem.dto.ValvulaFotoDto;
import com.mx.uvas.watersystem.model.ValvulaEntity;
import com.mx.uvas.watersystem.model.ValvulaFotoEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ValvulaMapper {
    public ValvulaDto entityToDto(ValvulaEntity entity) {
        var dto = new ValvulaDto();
        BeanUtils.copyProperties(entity, dto);
        if (entity.getCaja() != null) {
            dto.setCajaId(entity.getCaja().getCajaId());
        }
        if (entity.getFotos() != null) {
            dto.setFotos(entity.getFotos().stream()
                    .map(this::fotoEntityToDto)
                    .toList());
        }
        return dto;
    }

    public ValvulaFotoDto fotoEntityToDto(ValvulaFotoEntity entity) {
        var dto = new ValvulaFotoDto();
        dto.setFotoId(entity.getFotoId());
        dto.setNombreArchivo(entity.getNombreArchivo());
        dto.setNombreOriginal(entity.getNombreOriginal());
        dto.setContentType(entity.getContentType());
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getValvula() != null) {
            dto.setValvulaId(entity.getValvula().getValvulaId());
        }
        return dto;
    }

    public ValvulaEntity dtoToEntity(ValvulaDto dto) {
        return ValvulaEntity.builder()
                .identificador(dto.getIdentificador())
                .tipo(dto.getTipo())
                .estado(dto.getEstado())
                .observaciones(dto.getObservaciones())
                .build();
    }
}
