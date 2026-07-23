package com.mx.uvas.watersystem.mapping;

import com.mx.uvas.watersystem.dto.WaterEgresoDto;
import com.mx.uvas.watersystem.dto.WaterEgresoGastoDto;
import com.mx.uvas.watersystem.dto.WaterEgresoLineaDto;
import com.mx.uvas.watersystem.model.WaterEgresoEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class WaterEgresoMapper {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", new Locale("es", "MX"));

    public WaterEgresoDto entityToDto(WaterEgresoEntity entity) {
        var response = new WaterEgresoDto();
        BeanUtils.copyProperties(entity, response);

        if (entity.getFechaPago() != null) {
            response.setFechaPagoStr(entity.getFechaPago().format(dateFormatter));
        }
        if (entity.getFechaCompra() != null) {
            response.setFechaCompraStr(entity.getFechaCompra().format(dateFormatter));
        }
        if (entity.getTipoComprobante() != null) {
            response.setTipoComprobanteId(entity.getTipoComprobante().getCatalogoOpcionesId());
            response.setTipoComprobanteNombre(entity.getTipoComprobante().getNombre());
        }
        if (entity.getConcepto() != null) {
            response.setConceptoId(entity.getConcepto().getCatalogoOpcionesId());
            response.setConceptoNombre(entity.getConcepto().getNombre());
        }
        if (entity.getPersona() != null) {
            response.setPersonaId(entity.getPersona().getPersonaId());
            response.setPersonaNombre(String.join(" ",
                    nullSafe(entity.getPersona().getNombre()),
                    nullSafe(entity.getPersona().getNombre2()),
                    nullSafe(entity.getPersona().getApp()),
                    nullSafe(entity.getPersona().getApm())
            ).trim().replaceAll("\\s+", " "));
        }

        List<WaterEgresoLineaDto> lineas = Objects.isNull(entity.getLineas())
                ? List.of()
                : entity.getLineas().stream()
                .filter(l -> l.getEstatus() != null && l.getEstatus() == 1)
                .map(this::lineaEntityToDto)
                .toList();
        response.setLineas(lineas);

        return response;
    }

    public WaterEgresoLineaDto lineaEntityToDto(WaterEgresoEntity linea) {
        var dto = new WaterEgresoLineaDto();
        dto.setAguaEgresosId(linea.getAguaEgresosId());
        dto.setMonto(linea.getMonto());
        dto.setDescripcion(linea.getDescripcion());
        dto.setNoFolio(linea.getNoFolio());
        if (linea.getConcepto() != null) {
            dto.setConceptoId(linea.getConcepto().getCatalogoOpcionesId());
            dto.setConceptoNombre(linea.getConcepto().getNombre());
        }
        dto.setFechaPago(linea.getFechaPago());
        if (linea.getFechaPago() != null) {
            dto.setFechaPagoStr(linea.getFechaPago().format(dateFormatter));
        }
        dto.setProveedor(linea.getProveedor());
        if (linea.getPersona() != null) {
            dto.setPersonaId(linea.getPersona().getPersonaId());
            dto.setPersonaNombre(String.join(" ",
                    nullSafe(linea.getPersona().getNombre()),
                    nullSafe(linea.getPersona().getNombre2()),
                    nullSafe(linea.getPersona().getApp()),
                    nullSafe(linea.getPersona().getApm())
            ).trim().replaceAll("\\s+", " "));
        }

        // Recursivo: una línea puede a su vez tener sus propias sub-líneas
        // (dato histórico). No afecta las sumas existentes, solo se agrega
        // para consulta/detalle.
        if (linea.getLineas() != null) {
            List<WaterEgresoLineaDto> sublineas = linea.getLineas().stream()
                    .filter(l -> l.getEstatus() != null && l.getEstatus() == 1)
                    .map(this::lineaEntityToDto)
                    .toList();
            if (!sublineas.isEmpty()) {
                dto.setSublineas(sublineas);
            }
        }
        return dto;
    }

    public WaterEgresoGastoDto gastoEntityToDto(WaterEgresoEntity gasto) {
        var dto = new WaterEgresoGastoDto();
        dto.setAguaEgresosId(gasto.getAguaEgresosId());
        dto.setMonto(gasto.getMonto());
        dto.setDescripcion(gasto.getDescripcion());
        dto.setFechaPago(gasto.getFechaPago());
        if (gasto.getFechaPago() != null) {
            dto.setFechaPagoStr(gasto.getFechaPago().format(dateFormatter));
        }
        dto.setProveedor(gasto.getProveedor());
        if (gasto.getConcepto() != null) {
            dto.setConceptoId(gasto.getConcepto().getCatalogoOpcionesId());
            dto.setConceptoNombre(gasto.getConcepto().getNombre());
        }
        if (gasto.getPersona() != null) {
            dto.setPersonaId(gasto.getPersona().getPersonaId());
            dto.setPersonaNombre(String.join(" ",
                    nullSafe(gasto.getPersona().getNombre()),
                    nullSafe(gasto.getPersona().getNombre2()),
                    nullSafe(gasto.getPersona().getApp()),
                    nullSafe(gasto.getPersona().getApm())
            ).trim().replaceAll("\\s+", " "));
        }
        if (gasto.getTipoComprobante() != null) {
            dto.setTipoComprobanteId(gasto.getTipoComprobante().getCatalogoOpcionesId());
            dto.setTipoComprobanteNombre(gasto.getTipoComprobante().getNombre());
        }
        return dto;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
