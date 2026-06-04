package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.CatalogOptionsDto;
import com.mx.uvas.watersystem.dto.PersonDto;
import com.mx.uvas.watersystem.dto.ProcessDto;
import com.mx.uvas.watersystem.model.ProcessEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component
public class ProcessMapper {

    public ProcessDto entityToDto(ProcessEntity entity) {
        var response = new ProcessDto();
        BeanUtils.copyProperties(entity,response);
        var person = new PersonDto();
        BeanUtils.copyProperties((entity.getPerson() != null) ? entity.getPerson(): new PersonDto(),person);
        response.setPerson(person);
        var cat = new CatalogOptionsDto();
        BeanUtils.copyProperties((entity.getCatTipo() != null) ?  entity.getCatTipo():new CatalogOptionsDto(),cat);
        response.setTipoTramite(cat);
        cat = new CatalogOptionsDto();
        BeanUtils.copyProperties((entity.getCatEstatus()!= null) ?  entity.getCatEstatus():new CatalogOptionsDto(),cat);
        response.setEstatusTramite(cat);
        if (entity.getFechaEntrega() != null) { // Verifica si la fecha no es nula
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); // Formato deseado
            response.setFechaEntrega(dateFormat.format(entity.getFechaEntrega())); // Asignar la fecha formateada
        }
        return response;
    }
}
