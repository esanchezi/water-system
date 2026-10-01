package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.AvisoAdeudoDto;
import com.mx.uvas.watersystem.dto.AvisoAdeudoFotoDto;
import com.mx.uvas.watersystem.model.AvisoAdeudoEntity;
import com.mx.uvas.watersystem.model.AvisoAdeudoFotoEntity;
import org.springframework.stereotype.Component;

@Component
public class AvisoAdeudoMapper {

    public AvisoAdeudoDto entityToDto(AvisoAdeudoEntity entity) {
        var dto = new AvisoAdeudoDto();
        dto.setAvisoAdeudoId(entity.getAvisoAdeudoId());
        dto.setFolioNotificacion(entity.getFolioNotificacion());
        dto.setTipoAviso(entity.getTipoAviso());
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
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getWaterUser() != null) {
            dto.setAguaUsuarioId(entity.getWaterUser().getAguaUsuarioId());
            dto.setNoUsuario(entity.getWaterUser().getNoUsuario());
        }

        dto.setCancelada(entity.getEstatus() != null && entity.getEstatus() == 0);
        dto.setComentarioCancela(entity.getComentarioCancela());
        dto.setEntregado(entity.getFechaEntrega() != null);
        dto.setFechaEntrega(entity.getFechaEntrega());
        dto.setTipoEntrega(entity.getTipoEntrega());
        dto.setNombreReceptor(entity.getNombreReceptor());
        dto.setParentescoReceptor(entity.getParentescoReceptor());
        dto.setNombreNotificador(entity.getNombreNotificador());
        dto.setNombreTestigo1(entity.getNombreTestigo1());
        dto.setNombreTestigo2(entity.getNombreTestigo2());
        dto.setComentarioEntrega(entity.getComentarioEntrega());

        dto.setAtendido(entity.getFechaAtencion() != null);
        dto.setFechaAtencion(entity.getFechaAtencion());
        dto.setResultadoAtencion(entity.getResultadoAtencion());
        dto.setComentarioAtencion(entity.getComentarioAtencion());
        dto.setFolioReciboVinculado(entity.getFolioReciboVinculado());
        dto.setFolioConvenioVinculado(entity.getFolioConvenioVinculado());
        dto.setFechaCompromiso(entity.getFechaCompromiso());
        return dto;
    }

    // Fotos de respaldo de la entrega -- ver AvisoAdeudoFotoEntity.
    public AvisoAdeudoFotoDto fotoEntityToDto(AvisoAdeudoFotoEntity entity) {
        var dto = new AvisoAdeudoFotoDto();
        dto.setFotoId(entity.getFotoId());
        dto.setNombreArchivo(entity.getNombreArchivo());
        dto.setNombreOriginal(entity.getNombreOriginal());
        dto.setContentType(entity.getContentType());
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getAvisoAdeudo() != null) {
            dto.setAvisoAdeudoId(entity.getAvisoAdeudo().getAvisoAdeudoId());
        }
        return dto;
    }
}
