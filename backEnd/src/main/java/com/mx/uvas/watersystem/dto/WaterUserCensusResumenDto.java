package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WaterUserCensusResumenDto implements Serializable {
    private List<RangoEdadDto> rangos;
    private Integer sinClasificar;
    private Integer totalPersonas;

    // Mismo total, agrupado por calle/zona de la casa del usuario (Calle
    // Principal, La Barca, San Isidro, Buenavista, etc. -- lo que tengas
    // dado de alta en el catálogo de calles).
    private List<RangoEdadDto> porZona;
    private Integer sinZonaAsignada;

    // Cuántos usuarios están marcados como negocio (esNegocio = true),
    // agrupados por giro (catálogo NEGOCIO) -- no depende de la ficha del
    // censo, se calcula directo de la clasificación de uso de cada usuario.
    private Integer totalNegocios;
    private List<RangoEdadDto> porGiro;
    private Integer sinGiroAsignado;

    // Desglose de totalNegocios: cuántos ya son usuario formal (con cuota,
    // se les cobra) contra cuántos siguen solo en preregistro (nunca van a
    // convertirse en usuario, ej. tiendita atendida por el mismo usuario de
    // la casa) -- para nota de pie de página en el reporte.
    private Integer negociosConUsuario;
    private Integer negociosSinUsuario;
}
