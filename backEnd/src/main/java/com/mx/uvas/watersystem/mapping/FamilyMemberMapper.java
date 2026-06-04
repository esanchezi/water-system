package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.CatalogOptionsDto;
import com.mx.uvas.watersystem.dto.FamilyMemberDto;
import com.mx.uvas.watersystem.dto.PersonDto;
import com.mx.uvas.watersystem.dto.ProcessDto;
import com.mx.uvas.watersystem.model.FamilyMemberEntity;
import com.mx.uvas.watersystem.model.ProcessEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component
public class FamilyMemberMapper {

    public FamilyMemberDto entityToDto(FamilyMemberEntity entity) {
        var response = new FamilyMemberDto();
        BeanUtils.copyProperties(entity,response);
        var person = new PersonDto();
        BeanUtils.copyProperties((entity.getPerson() != null) ? entity.getPerson(): new PersonDto(),person);
        response.setPerson(person);
        var cat = new CatalogOptionsDto();
        BeanUtils.copyProperties((entity.getCatTipo() != null) ?  entity.getCatTipo():new CatalogOptionsDto(),cat);
        response.setTipoFamilia(cat);
        return response;
    }
}
