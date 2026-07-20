package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

// Reporte pivote: columnas = conceptos de cobro (catalogo_id = 2),
// filas = años (agua_recibo.fecha = cuándo se recaudó, no el año de la
// cuota). Solo considera pagos en efectivo (tipo_pago_id = 16) de recibos
// válidos (agua_recibo.invalido IS NULL) con folio > 4099, sumando
// agua_recibo_pago.monto_aplicado. También incluye egresos de nivel 1
// (agua_egresos) y el efectivo que debería haber por año (no se totaliza
// a nivel general, solo por año, por indicación del usuario).
@Data
public class TotalPorAnioDto implements Serializable {
    private List<ConceptoResumenDto> conceptos;
    private List<TotalPorAnioFilaDto> filas;
    private Map<Integer, Double> totalesPorConcepto;
    private Double granTotal;
    private Double granEgresos;
}
