package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.ReciboRevisionDto;
import com.mx.uvas.watersystem.dto.ReciboRevisionFotoDto;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.ReciboRevisionEntity;
import com.mx.uvas.watersystem.model.ReciboRevisionFotoEntity;
import com.mx.uvas.watersystem.model.WaterReceiptEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class ReciboRevisionMapper {

    public ReciboRevisionFotoDto fotoEntityToDto(ReciboRevisionFotoEntity entity) {
        var dto = new ReciboRevisionFotoDto();
        dto.setFotoId(entity.getFotoId());
        dto.setNombreArchivo(entity.getNombreArchivo());
        dto.setNombreOriginal(entity.getNombreOriginal());
        dto.setContentType(entity.getContentType());
        dto.setObservaciones(entity.getObservaciones());
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getRevisiones() != null) {
            dto.setRevisiones(entity.getRevisiones().stream()
                    .map(this::revisionEntityToDto)
                    .sorted(Comparator.comparing(ReciboRevisionDto::getRevisionId,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList());
        }
        return dto;
    }

    public ReciboRevisionDto revisionEntityToDto(ReciboRevisionEntity entity) {
        var dto = new ReciboRevisionDto();
        dto.setRevisionId(entity.getRevisionId());
        dto.setNoFolioCapturado(entity.getNoFolioCapturado());
        dto.setNoUsuarioCapturado(entity.getNoUsuarioCapturado());
        dto.setMontoTexto(entity.getMontoTexto());
        dto.setObservaciones(entity.getObservaciones());
        dto.setResultado(entity.getResultado());
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getFoto() != null) {
            dto.setFotoId(entity.getFoto().getFotoId());
        }
        WaterReceiptEntity recibo = entity.getWaterReceipt();
        if (recibo != null) {
            dto.setReciboId(recibo.getAguaReciboId());
            dto.setNoFolioSistema(recibo.getNoFolio());
            dto.setMontoSistema(recibo.getTotal());
            dto.setConceptoSistema(recibo.getConcepto());
            dto.setFechaSistema(recibo.getFecha());
            WaterUserEntity usuario = recibo.getWaterUser();
            if (usuario != null) {
                dto.setNoUsuarioSistema(usuario.getNoUsuario());
                dto.setNombreUsuarioSistema(nombreCompleto(usuario.getPerson()));
            }
        }
        return dto;
    }

    private String nombreCompleto(PersonEntity persona) {
        if (persona == null) {
            return null;
        }
        return String.join(" ",
                        java.util.stream.Stream.of(persona.getNombre(), persona.getNombre2(),
                                        persona.getApp(), persona.getApm())
                                .filter(s -> s != null && !s.isBlank())
                                .toList())
                .trim();
    }
}
