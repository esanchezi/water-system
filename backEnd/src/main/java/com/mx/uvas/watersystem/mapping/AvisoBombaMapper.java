package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.AvisoBombaDto;
import com.mx.uvas.watersystem.model.AvisoBombaEntity;
import org.springframework.stereotype.Component;

@Component
public class AvisoBombaMapper {

    public AvisoBombaDto entityToDto(AvisoBombaEntity entity) {
        var dto = new AvisoBombaDto();
        dto.setAvisoBombaId(entity.getAvisoBombaId());
        dto.setFolioNotificacion(entity.getFolioNotificacion());
        dto.setNombreUsuarioTitular(entity.getNombreUsuarioTitular());
        dto.setNoCasa(entity.getNoCasa());
        dto.setNoCasaTexto(entity.getNoCasaTexto());
        dto.setDomicilioToma(entity.getDomicilioToma());
        dto.setFechaReporte(entity.getFechaReporte());
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getWaterUser() != null) {
            dto.setAguaUsuarioId(entity.getWaterUser().getAguaUsuarioId());
            dto.setNoUsuario(entity.getWaterUser().getNoUsuario());
        }

        dto.setCancelado(entity.getEstatus() != null && entity.getEstatus() == 0);
        dto.setEntregado(entity.getFechaEntrega() != null);
        dto.setFechaEntrega(entity.getFechaEntrega());
        dto.setTipoEntrega(entity.getTipoEntrega());
        dto.setNombreReceptor(entity.getNombreReceptor());
        dto.setParentescoReceptor(entity.getParentescoReceptor());
        dto.setNombreNotificador(entity.getNombreNotificador());
        dto.setNombreTestigo1(entity.getNombreTestigo1());
        dto.setNombreTestigo2(entity.getNombreTestigo2());
        return dto;
    }
}
