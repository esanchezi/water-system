package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class WaterAgreementDto implements Serializable {
    private Integer aguaConvenioId;
    private Integer noFolio;
    private Integer noUsuario;
    private String nombreUsuario;
    private LocalDate fecha;
    private String fechaStr;
    private String motivo;
    private String comentario;
    private Integer estatusConvenioId;
    private CatalogOptionsDto estatusConvenio;
    private List<WaterAgreementChargeDto> cargos;
    private Double montoCondonadoTotal;
}
