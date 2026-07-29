package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

// Captura de un gasto suelto, sin vale todavía (nivel = 2, padre_id = null).
// Al emitir el vale de fin de mes, este gasto se re-parenta a la cabecera
// que se crea en ese momento.
// tipoComprobanteId es opcional: si el gasto ya tiene nota/factura propia se
// captura aquí desde el inicio (Factura, Nota, Remisión); si no tiene nada en
// físico, se deja vacío y ese gasto es justo el tipo de cosa que se junta en
// un "Vale caja" al emitir (el tipo de comprobante del vale resultante sí se
// captura en ese momento, en el paso de Emitir vale).
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
    private Integer tipoComprobanteId;
    private String tipoComprobanteNombre;
}
