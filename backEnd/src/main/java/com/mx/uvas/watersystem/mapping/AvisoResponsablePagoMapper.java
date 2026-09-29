package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.AvisoResponsablePagoDto;
import com.mx.uvas.watersystem.dto.AvisoResponsablePagoFotoDto;
import com.mx.uvas.watersystem.dto.AvisoResponsablePagoPersonaDto;
import com.mx.uvas.watersystem.model.AvisoResponsablePagoEntity;
import com.mx.uvas.watersystem.model.AvisoResponsablePagoFotoEntity;
import com.mx.uvas.watersystem.model.AvisoResponsablePagoPersonaEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class AvisoResponsablePagoMapper {

    public AvisoResponsablePagoDto entityToDto(AvisoResponsablePagoEntity entity) {
        var dto = new AvisoResponsablePagoDto();
        dto.setResponsablePagoId(entity.getResponsablePagoId());
        dto.setFolioNotificacion(entity.getFolioNotificacion());
        dto.setNoCasa(entity.getNoCasa());
        dto.setNoCasaTexto(entity.getNoCasaTexto());
        dto.setDomicilioToma(entity.getDomicilioToma());
        dto.setMotivoSolicitud(entity.getMotivoSolicitud());
        dto.setFechaSolicitud(entity.getFechaSolicitud());
        dto.setObservacionesComite(entity.getObservacionesComite());
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getCasa() != null) {
            dto.setCasaId(entity.getCasa().getCasaId());
        }

        List<AvisoResponsablePagoPersonaDto> personas = entity.getPersonas() != null
                ? entity.getPersonas().stream()
                        .sorted(Comparator.comparing(AvisoResponsablePagoPersonaEntity::getOrden,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(this::personaEntityToDto)
                        .toList()
                : List.of();
        dto.setPersonas(personas);

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

    private AvisoResponsablePagoPersonaDto personaEntityToDto(AvisoResponsablePagoPersonaEntity entity) {
        var dto = new AvisoResponsablePagoPersonaDto();
        dto.setResponsablePagoPersonaId(entity.getResponsablePagoPersonaId());
        dto.setOrden(entity.getOrden());
        dto.setNombreCompleto(entity.getNombreCompleto());
        dto.setParentesco(entity.getParentesco());
        dto.setFamiliaCuota(entity.getFamiliaCuota());
        dto.setCuotaVigenteSnapshot(entity.getCuotaVigenteSnapshot());
        if (entity.getWaterUser() != null) {
            dto.setAguaUsuarioId(entity.getWaterUser().getAguaUsuarioId());
            dto.setNoUsuario(entity.getWaterUser().getNoUsuario());
        }
        return dto;
    }

    // Fotos de respaldo de la entrega -- ver AvisoResponsablePagoFotoEntity.
    public AvisoResponsablePagoFotoDto fotoEntityToDto(AvisoResponsablePagoFotoEntity entity) {
        var dto = new AvisoResponsablePagoFotoDto();
        dto.setFotoId(entity.getFotoId());
        dto.setNombreArchivo(entity.getNombreArchivo());
        dto.setNombreOriginal(entity.getNombreOriginal());
        dto.setContentType(entity.getContentType());
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getResponsablePago() != null) {
            dto.setResponsablePagoId(entity.getResponsablePago().getResponsablePagoId());
        }
        return dto;
    }
}
