package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.RegistroSuministroDto;
import com.mx.uvas.watersystem.dto.RegistroSuministroTramoDto;
import com.mx.uvas.watersystem.model.RegistroSuministroEntity;
import com.mx.uvas.watersystem.model.RegistroSuministroTramoEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class RegistroSuministroMapper {

    public RegistroSuministroDto entityToDto(RegistroSuministroEntity entity) {
        var dto = new RegistroSuministroDto();
        BeanUtils.copyProperties(entity, dto);

        if (entity.getPozo() != null) {
            dto.setPozoId(entity.getPozo().getPozoId());
            dto.setPozoNombre(entity.getPozo().getNombre());
        }

        if (entity.getListTramos() != null) {
            dto.setListTramos(entity.getListTramos().stream()
                    .map(this::tramoEntityToDto)
                    .toList());
        }
        return dto;
    }

    private RegistroSuministroTramoDto tramoEntityToDto(RegistroSuministroTramoEntity entity) {
        var dto = new RegistroSuministroTramoDto();
        dto.setRegistroTramoId(entity.getRegistroTramoId());
        dto.setOrden(entity.getOrden());
        if (entity.getTramo() != null) {
            dto.setTramoId(entity.getTramo().getTramoId());
            dto.setTramoNombre(entity.getTramo().getNombre());
        }
        return dto;
    }
}
