package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.WaterUserNoticeDto;
import com.mx.uvas.watersystem.model.WaterUserNoticeEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class WaterUserNoticeMapper {

    private final CatalogMapper catalogMapper;

    public WaterUserNoticeMapper(CatalogMapper catalogMapper) {
        this.catalogMapper = catalogMapper;
    }

    public WaterUserNoticeDto entityToDto(WaterUserNoticeEntity entity) {
        var response = new WaterUserNoticeDto();
        BeanUtils.copyProperties(entity,response);

        if (entity.getWaterUser() != null) {
            response.setNoUsuario(entity.getWaterUser().getNoUsuario());
        }
        if (entity.getEstatusAviso() != null) {
            response.setAvisoEstatusId(entity.getEstatusAviso().getCatalogoOpcionesId());
            response.setEstatusAviso(catalogMapper.optionEntityToDto(entity.getEstatusAviso()));
        }
        if (entity.getTipo() != null) {
            response.setTipoId(entity.getTipo().getCatalogoOpcionesId());
            response.setTipo(catalogMapper.optionEntityToDto(entity.getTipo()));
        }
        if (entity.getResponsable() != null) {
            response.setResponsableId(entity.getResponsable().getCatalogoOpcionesId());
            response.setResponsable(catalogMapper.optionEntityToDto(entity.getResponsable()));
        }

        return response;
    }
}
