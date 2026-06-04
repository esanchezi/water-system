package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.CatalogDto;
import com.mx.uvas.watersystem.dto.CatalogOptionsDto;
import com.mx.uvas.watersystem.model.CatalogEntity;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class CatalogMapper {
    public CatalogDto entityToDto(CatalogEntity entity) {
        var response = new CatalogDto();
        BeanUtils.copyProperties(entity,response);
        for (CatalogOptionsEntity options : entity.getCatalogOptions()) {
            var option = new CatalogOptionsDto();
            BeanUtils.copyProperties(options,option);
            response.getOptions().add(option);
        }

        return response;
    }
}
