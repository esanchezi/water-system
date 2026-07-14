package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class WaterUserChargeDto implements Serializable {
    private Integer aguaUsuarioCargoId;
    private Integer noUsuario;
    private Integer conceptoId;
    private CatalogOptionsDto concepto;
    private String descripcion;
    private Double monto;
    private LocalDate fecha;
    private String fechaStr;
    private String comentario;
    private List<WaterUserChargePaymentDto> pagos;
    private Double montoPagado;
    private Double montoCondonado;
    private Double saldo;
    private Boolean pagado;
}
