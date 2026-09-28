package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.RenunciaTemporalDto;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.RenunciaTemporalEntity;
import org.springframework.stereotype.Component;

@Component
public class RenunciaTemporalMapper {

    public RenunciaTemporalDto entityToDto(RenunciaTemporalEntity entity) {
        RenunciaTemporalDto dto = new RenunciaTemporalDto();
        dto.setRenunciaTemporalId(entity.getRenunciaTemporalId());
        dto.setFolio(entity.getFolio());
        dto.setFechaRenuncia(entity.getFechaRenuncia());
        dto.setMotivo(entity.getMotivo());
        dto.setAdeudoALaFecha(entity.getAdeudoALaFecha());
        dto.setCancelada(entity.getEstatus() != null && entity.getEstatus() == 0);
        dto.setFechaSolicitudReconexion(entity.getFechaSolicitudReconexion());
        dto.setFechaAsamblea(entity.getFechaAsamblea());
        dto.setCondicionesReconexion(entity.getCondicionesReconexion());
        dto.setFechaReconexion(entity.getFechaReconexion());
        dto.setReconectado(entity.getFechaReconexion() != null);
        dto.setDateAdd(entity.getDateAdd());
        if (entity.getWaterUser() != null) {
            dto.setNoUsuario(entity.getWaterUser().getNoUsuario());
            dto.setNombreUsuario(buildNombreCompleto(entity.getWaterUser().getPerson()));
        }
        return dto;
    }

    private String buildNombreCompleto(PersonEntity person) {
        if (person == null) return "";
        return String.join(" ",
                        nullToEmpty(person.getNombre()), nullToEmpty(person.getNombre2()),
                        nullToEmpty(person.getApp()), nullToEmpty(person.getApm()))
                .replaceAll("\\s+", " ").trim();
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
