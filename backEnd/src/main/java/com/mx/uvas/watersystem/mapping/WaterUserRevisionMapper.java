package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.WaterUserRevisionDto;
import com.mx.uvas.watersystem.dto.WaterUserRevisionFotoDto;
import com.mx.uvas.watersystem.model.WaterUserRevisionEntity;
import com.mx.uvas.watersystem.model.WaterUserRevisionFotoEntity;
import org.springframework.stereotype.Component;

@Component
public class WaterUserRevisionMapper {

    public WaterUserRevisionDto entityToDto(WaterUserRevisionEntity entity) {
        WaterUserRevisionDto dto = new WaterUserRevisionDto();
        dto.setRevisionId(entity.getRevisionId());
        dto.setFecha(entity.getFecha());
        dto.setTipo(entity.getTipo());
        dto.setResultado(entity.getResultado());
        dto.setComentario(entity.getComentario());
        dto.setUserIdAdd(entity.getUserIdAdd());
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getWaterUser() != null) {
            dto.setAguaUsuarioId(entity.getWaterUser().getAguaUsuarioId());
        }
        return dto;
    }

    public WaterUserRevisionFotoDto fotoEntityToDto(WaterUserRevisionFotoEntity entity) {
        WaterUserRevisionFotoDto dto = new WaterUserRevisionFotoDto();
        dto.setFotoId(entity.getFotoId());
        dto.setNombreArchivo(entity.getNombreArchivo());
        dto.setNombreOriginal(entity.getNombreOriginal());
        dto.setContentType(entity.getContentType());
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getRevision() != null) {
            dto.setRevisionId(entity.getRevision().getRevisionId());
        }
        return dto;
    }
}
