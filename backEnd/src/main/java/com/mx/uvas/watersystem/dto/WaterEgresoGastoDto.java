package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

// Captura de un gasto suelto, sin vale todavía (nivel = 2, padre_id = null).
// Al emitir el vale de fin de mes, este gasto se re-parenta a la cabecera
// que se crea en ese momento.
@Data
public class WaterEgresoGastoDto implements Serializable {
    private Integer aguaEgresosId;
    private LocalDate fechaPago;
    private String fechaPagoStr;
    private Integer conceptoId;
    private String conceptoNombre;
    private Double monto;
    private String descripcion;
    private String proveedor;
    private Integer personaId;
    private String personaNombre;
}
