package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.SegmentoTuberiaDto;
import com.mx.uvas.watersystem.dto.TramoDto;
import com.mx.uvas.watersystem.dto.TramoHorarioDto;
import com.mx.uvas.watersystem.dto.TramoValvulaDto;
import com.mx.uvas.watersystem.model.SegmentoTuberiaEntity;
import com.mx.uvas.watersystem.model.TramoEntity;
import com.mx.uvas.watersystem.model.TramoHorarioEntity;
import com.mx.uvas.watersystem.model.TramoValvulaEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class TramoMapper {
    public TramoDto entityToDto(TramoEntity entity) {
        var dto = new TramoDto();
        BeanUtils.copyProperties(entity, dto);
        if (entity.getHorarios() != null) {
            dto.setHorarios(entity.getHorarios().stream()
                    .map(this::horarioEntityToDto)
                    .toList());
        }
        if (entity.getValvulas() != null) {
            dto.setValvulas(entity.getValvulas().stream()
                    .map(this::valvulaEntityToDto)
                    .toList());
        }
        if (entity.getSegmentos() != null) {
            dto.setSegmentos(entity.getSegmentos().stream()
                    .sorted(Comparator.comparing(SegmentoTuberiaEntity::getOrden,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(this::segmentoEntityToDto)
                    .toList());
        }
        return dto;
    }

    private SegmentoTuberiaDto segmentoEntityToDto(SegmentoTuberiaEntity entity) {
        var dto = new SegmentoTuberiaDto();
        dto.setSegmentoId(entity.getSegmentoId());
        dto.setOrden(entity.getOrden());
        dto.setMaterial(entity.getMaterial());
        dto.setDiametro(entity.getDiametro());
        dto.setLongitudMetros(entity.getLongitudMetros());
        dto.setObservaciones(entity.getObservaciones());
        return dto;
    }

    private TramoHorarioDto horarioEntityToDto(TramoHorarioEntity entity) {
        var dto = new TramoHorarioDto();
        dto.setHorarioId(entity.getHorarioId());
        dto.setDiaSemana(entity.getDiaSemana());
        dto.setHoraInicio(entity.getHoraInicio());
        dto.setHoraFin(entity.getHoraFin());
        if (entity.getPozo() != null) {
            dto.setPozoId(entity.getPozo().getPozoId());
            dto.setPozoNombre(entity.getPozo().getNombre());
        }
        return dto;
    }

    private TramoValvulaDto valvulaEntityToDto(TramoValvulaEntity entity) {
        var dto = new TramoValvulaDto();
        dto.setTramoValvulaId(entity.getTramoValvulaId());
        dto.setEstadoRequerido(entity.getEstadoRequerido());
        if (entity.getValvula() != null) {
            dto.setValvulaId(entity.getValvula().getValvulaId());
            dto.setValvulaIdentificador(entity.getValvula().getIdentificador());
            if (entity.getValvula().getCaja() != null) {
                dto.setCajaId(entity.getValvula().getCaja().getCajaId());
                dto.setCajaNombre(entity.getValvula().getCaja().getNombre());
                dto.setCajaLado(entity.getValvula().getCaja().getLado());
            }
        }
        return dto;
    }

    public TramoEntity dtoToEntity(TramoDto dto) {
        return TramoEntity.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .lado(dto.getLado())
                .trazoJson(dto.getTrazoJson())
                .metodoTrazo(dto.getMetodoTrazo())
                .build();
    }
}
