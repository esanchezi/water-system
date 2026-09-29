package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.AvisoPadronDto;
import com.mx.uvas.watersystem.dto.AvisoPadronFotoDto;
import com.mx.uvas.watersystem.model.AvisoPadronEntity;
import com.mx.uvas.watersystem.model.AvisoPadronFotoEntity;
import org.springframework.stereotype.Component;

@Component
public class AvisoPadronMapper {

    public AvisoPadronDto entityToDto(AvisoPadronEntity entity) {
        var dto = new AvisoPadronDto();
        dto.setAvisoPadronId(entity.getAvisoPadronId());
        dto.setFolioNotificacion(entity.getFolioNotificacion());
        dto.setNombreUsuarioTitular(entity.getNombreUsuarioTitular());
        dto.setNoCasa(entity.getNoCasa());
        dto.setNoCasaTexto(entity.getNoCasaTexto());
        dto.setDomicilioToma(entity.getDomicilioToma());
        dto.setFechaPresentacion(entity.getFechaPresentacion());
        dto.setMotivoSolicitud(entity.getMotivoSolicitud());
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
        dto.setComentarioEntrega(entity.getComentarioEntrega());
        return dto;
    }

    // Fotos de respaldo de la entrega -- ver AvisoPadronFotoEntity.
    public AvisoPadronFotoDto fotoEntityToDto(AvisoPadronFotoEntity entity) {
        var dto = new AvisoPadronFotoDto();
        dto.setFotoId(entity.getFotoId());
        dto.setNombreArchivo(entity.getNombreArchivo());
        dto.setNombreOriginal(entity.getNombreOriginal());
        dto.setContentType(entity.getContentType());
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getAvisoPadron() != null) {
            dto.setAvisoPadronId(entity.getAvisoPadron().getAvisoPadronId());
        }
        return dto;
    }
}
