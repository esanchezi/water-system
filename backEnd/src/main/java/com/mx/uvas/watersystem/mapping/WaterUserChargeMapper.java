package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.WaterUserChargeDto;
import com.mx.uvas.watersystem.dto.WaterUserChargePaymentDto;
import com.mx.uvas.watersystem.model.WaterUserChargeEntity;
import com.mx.uvas.watersystem.model.WaterUserChargePaymentEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class WaterUserChargeMapper {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", new Locale("es", "MX"));
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm", new Locale("es", "MX"));
    private final CatalogMapper catalogMapper;

    public WaterUserChargeMapper(CatalogMapper catalogMapper) {
        this.catalogMapper = catalogMapper;
    }

    public WaterUserChargeDto entityToDto(WaterUserChargeEntity entity) {
        var response = new WaterUserChargeDto();
        BeanUtils.copyProperties(entity, response);

        if (entity.getWaterUser() != null) {
            response.setNoUsuario(entity.getWaterUser().getNoUsuario());
        }

        if (entity.getConcepto() != null) {
            response.setConceptoId(entity.getConcepto().getCatalogoOpcionesId());
            response.setConcepto(catalogMapper.optionEntityToDto(entity.getConcepto()));
        }

        if (entity.getFecha() != null) {
            response.setFechaStr(entity.getFecha().format(dateFormatter));
        }

        List<WaterUserChargePaymentDto> pagos = Objects.isNull(entity.getPagos())
                ? List.of()
                : entity.getPagos().stream()
                .filter(p -> p.getEstatus() != null && p.getEstatus() == 1)
                .sorted(Comparator.comparing(WaterUserChargePaymentEntity::getFechaPago,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::paymentEntityToDto)
                .toList();

        response.setPagos(pagos);
        response.setMontoPagado(entity.getMontoPagado());
        response.setMontoCondonado(entity.getMontoCondonado());
        response.setSaldo(entity.getSaldo());
        response.setPagado(entity.isPagado());

        return response;
    }

    public WaterUserChargePaymentDto paymentEntityToDto(WaterUserChargePaymentEntity entity) {
        var response = new WaterUserChargePaymentDto();
        BeanUtils.copyProperties(entity, response);

        if (entity.getCargo() != null) {
            response.setAguaUsuarioCargoId(entity.getCargo().getAguaUsuarioCargoId());
        }
        if (entity.getWaterReceipt() != null) {
            response.setReciboId(entity.getWaterReceipt().getAguaReciboId());
            response.setNoFolio(entity.getWaterReceipt().getNoFolio());
        }
        if (entity.getFechaPago() != null) {
            response.setFechaPagoStr(entity.getFechaPago().format(dateTimeFormatter));
        }

        return response;
    }
}
