package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class WaterEgresoLineaDto implements Serializable {
    private Integer aguaEgresosId;
    private Integer conceptoId;
    private String conceptoNombre;
    private Double monto;
    private String descripcion;

    // Fecha real del gasto (no siempre es la misma que la del vale que lo
    // consolida: un gasto suelto conserva la fecha en la que se capturó).
    private LocalDate fechaPago;
    private String fechaPagoStr;

    // Proveedor/persona propios de esta línea (se llenan cuando la línea
    // viene de un gasto suelto emitido; si la línea viene del alta directa
    // de un vale con varias categorías, quedan vacíos y el frontend usa el
    // proveedor de la cabecera como respaldo).
    private String proveedor;
    private Integer personaId;
    private String personaNombre;

    // Folio propio de esta línea (no el del vale que la agrupa). Aplica a
    // registros históricos donde cada compra individual traía su propio
    // comprobante/folio, distinto del folio general del vale de caja.
    private String noFolio;

    // Desglose interno de esta línea, cuando el monto de la línea es a su vez
    // la suma de varias compras/artículos (dato histórico: antes de este
    // rediseño, algunas líneas ya venían agrupadas). Casi siempre vacío; solo
    // aplica a los pocos casos donde un mismo comprobante se desglosó en la
    // captura original. Solo para consulta/detalle: nunca se vuelve a sumar
    // aparte del monto de la línea que lo contiene.
    private List<WaterEgresoLineaDto> sublineas;
}
