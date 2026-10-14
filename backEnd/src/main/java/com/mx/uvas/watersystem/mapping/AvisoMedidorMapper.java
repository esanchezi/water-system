package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.AvisoMedidorDto;
import com.mx.uvas.watersystem.dto.AvisoMedidorFotoDto;
import com.mx.uvas.watersystem.model.AvisoMedidorEntity;
import com.mx.uvas.watersystem.model.AvisoMedidorFotoEntity;
import org.springframework.stereotype.Component;

@Component
public class AvisoMedidorMapper {

    public AvisoMedidorDto entityToDto(AvisoMedidorEntity entity) {
        var dto = new AvisoMedidorDto();
        dto.setAvisoMedidorId(entity.getAvisoMedidorId());
        dto.setFolioNotificacion(entity.getFolioNotificacion());
        dto.setNombreUsuarioTitular(entity.getNombreUsuarioTitular());
        dto.setNoCasa(entity.getNoCasa());
        dto.setNoCasaTexto(entity.getNoCasaTexto());
        dto.setDomicilioToma(entity.getDomicilioToma());
        dto.setFechaInstalacion(entity.getFechaInstalacion());
        dto.setNoSerieMedidor(entity.getNoSerieMedidor());
        dto.setLecturaInicialMedidor(entity.getLecturaInicialMedidor());
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

    // Fotos de respaldo de la entrega -- ver AvisoMedidorFotoEntity.
    public AvisoMedidorFotoDto fotoEntityToDto(AvisoMedidorFotoEntity entity) {
        var dto = new AvisoMedidorFotoDto();
        dto.setFotoId(entity.getFotoId());
        dto.setNombreArchivo(entity.getNombreArchivo());
        dto.setNombreOriginal(entity.getNombreOriginal());
        dto.setContentType(entity.getContentType());
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getAvisoMedidor() != null) {
            dto.setAvisoMedidorId(entity.getAvisoMedidor().getAvisoMedidorId());
        }
        return dto;
    }
}
