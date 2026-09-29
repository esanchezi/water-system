package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.AvisoInformativoAdeudoDto;
import com.mx.uvas.watersystem.dto.AvisoInformativoAdeudoFotoDto;
import com.mx.uvas.watersystem.model.AvisoInformativoAdeudoEntity;
import com.mx.uvas.watersystem.model.AvisoInformativoAdeudoFotoEntity;
import org.springframework.stereotype.Component;

@Component
public class AvisoInformativoAdeudoMapper {

    public AvisoInformativoAdeudoDto entityToDto(AvisoInformativoAdeudoEntity entity) {
        var dto = new AvisoInformativoAdeudoDto();
        dto.setAvisoInformativoAdeudoId(entity.getAvisoInformativoAdeudoId());
        dto.setFolioNotificacion(entity.getFolioNotificacion());
        dto.setNombreUsuarioTitular(entity.getNombreUsuarioTitular());
        dto.setNoCasa(entity.getNoCasa());
        dto.setNoCasaTexto(entity.getNoCasaTexto());
        dto.setDomicilioToma(entity.getDomicilioToma());
        dto.setPeriodosAdeudados(entity.getPeriodosAdeudados());
        dto.setAdeudoTotal(entity.getAdeudoTotal());
        dto.setMultaAcumulada(entity.getMultaAcumulada());
        dto.setNoFolioUltimoPago(entity.getNoFolioUltimoPago());
        dto.setFechaUltimoPago(entity.getFechaUltimoPago());
        dto.setFechaPresentacion(entity.getFechaPresentacion());
        dto.setObservacion(entity.getObservacion());
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

    public AvisoInformativoAdeudoFotoDto fotoEntityToDto(AvisoInformativoAdeudoFotoEntity entity) {
        var dto = new AvisoInformativoAdeudoFotoDto();
        dto.setFotoId(entity.getFotoId());
        dto.setNombreArchivo(entity.getNombreArchivo());
        dto.setNombreOriginal(entity.getNombreOriginal());
        dto.setContentType(entity.getContentType());
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getAvisoInformativoAdeudo() != null) {
            dto.setAvisoInformativoAdeudoId(entity.getAvisoInformativoAdeudo().getAvisoInformativoAdeudoId());
        }
        return dto;
    }
}
