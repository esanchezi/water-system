package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.WaterAgreementChargeDto;
import com.mx.uvas.watersystem.dto.WaterAgreementDto;
import com.mx.uvas.watersystem.model.WaterAgreementChargeEntity;
import com.mx.uvas.watersystem.model.WaterAgreementEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class WaterAgreementMapper {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", new Locale("es", "MX"));
    private final CatalogMapper catalogMapper;

    public WaterAgreementMapper(CatalogMapper catalogMapper) {
        this.catalogMapper = catalogMapper;
    }

    public WaterAgreementDto entityToDto(WaterAgreementEntity entity) {
        var response = new WaterAgreementDto();
        BeanUtils.copyProperties(entity, response);

        if (entity.getWaterUser() != null) {
            response.setNoUsuario(entity.getWaterUser().getNoUsuario());
            if (entity.getWaterUser().getPerson() != null) {
                var p = entity.getWaterUser().getPerson();
                response.setNombreUsuario(String.join(" ",
                        nullSafe(p.getNombre()), nullSafe(p.getNombre2()), nullSafe(p.getApp()), nullSafe(p.getApm())
                ).trim().replaceAll("\\s+", " "));
            }
        }

        if (entity.getEstatusConvenio() != null) {
            response.setEstatusConvenioId(entity.getEstatusConvenio().getCatalogoOpcionesId());
            response.setEstatusConvenio(catalogMapper.optionEntityToDto(entity.getEstatusConvenio()));
        }

        if (entity.getFecha() != null) {
            response.setFechaStr(entity.getFecha().format(dateFormatter));
        }

        if (entity.getFechaCompromisoPago() != null) {
            response.setFechaCompromisoPagoStr(entity.getFechaCompromisoPago().format(dateFormatter));
        }

        List<WaterAgreementChargeDto> cargos = Objects.isNull(entity.getCargos())
                ? List.of()
                : entity.getCargos().stream()
                .filter(c -> c.getEstatus() != null && c.getEstatus() == 1)
                .map(this::chargeEntityToDto)
                .toList();

        response.setCargos(cargos);
        response.setMontoCondonadoTotal(entity.getMontoCondonadoTotal());

        return response;
    }

    public WaterAgreementChargeDto chargeEntityToDto(WaterAgreementChargeEntity entity) {
        var response = new WaterAgreementChargeDto();
        BeanUtils.copyProperties(entity, response);

        if (entity.getConvenio() != null) {
            response.setAguaConvenioId(entity.getConvenio().getAguaConvenioId());
        }
        if (entity.getCargo() != null) {
            response.setAguaUsuarioCargoId(entity.getCargo().getAguaUsuarioCargoId());
            response.setMontoCargo(entity.getCargo().getMonto());
            if (entity.getCargo().getConcepto() != null) {
                response.setConceptoCargo(entity.getCargo().getConcepto().getNombre());
            }
        }

        return response;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
