package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.CatalogDto;
import com.mx.uvas.watersystem.dto.CatalogOptionsDto;
import com.mx.uvas.watersystem.dto.WaterUserNoticeDto;
import com.mx.uvas.watersystem.model.CatalogEntity;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.WaterUserNoticeEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class WaterUserNoticeMapper {
    public WaterUserNoticeDto entityToDto(WaterUserNoticeEntity entity) {
        var response = new WaterUserNoticeDto();
        BeanUtils.copyProperties(entity,response);

        return response;
    }
}
