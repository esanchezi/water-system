package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.CatalogDto;
import com.mx.uvas.watersystem.dto.CatalogOptionsDto;
import com.mx.uvas.watersystem.model.CatalogEntity;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class CatalogMapper {

    // ── Entity → Dto ────────────────────────────────────────────────────────
    public CatalogDto entityToDto(CatalogEntity entity) {
        CatalogDto dto = new CatalogDto();
        BeanUtils.copyProperties(entity, dto);
        if (entity.getCatalogOptions() != null) {
            entity.getCatalogOptions().stream()
                    .filter(o -> o.getEstatus() != null && o.getEstatus() == 1)
                    .forEach(o -> dto.getOptions().add(optionEntityToDto(o)));
        }
        return dto;
    }

    public CatalogOptionsDto optionEntityToDto(CatalogOptionsEntity entity) {
        CatalogOptionsDto dto = new CatalogOptionsDto();
        BeanUtils.copyProperties(entity, dto);
        if (entity.getCatalog() != null) {
            dto.setCatalogoId(entity.getCatalog().getCatalogoId());
        }
        return dto;
    }

    // ── Dto → Entity (create) ────────────────────────────────────────────────
    public CatalogEntity dtoToEntity(CatalogDto dto) {
        return CatalogEntity.builder()
                .clave(dto.getClave().toUpperCase().trim())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .estatus(1)
                .userIdAdd(1)   // TODO: Keycloak
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    public CatalogOptionsEntity optionDtoToEntity(CatalogOptionsDto dto, CatalogEntity catalog) {
        return CatalogOptionsEntity.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .estatus(1)
                .userIdAdd(1)   // TODO: Keycloak
                .dateAdd(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .catalog(catalog)
                .build();
    }
}
