package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

// Lo que se captura al marcar una carta como atendida (ya se hizo el
// cobro o el trámite correspondiente tras la entrega) -- ver
// AvisoAdeudoService.marcarAtendida().
@Data
public class AvisoAdeudoAtencionRequestDto implements Serializable {
    // "PAGADO" | "CONDONADO" | "CONVENIO" | "OTRO" -- obligatorio, ver
    // AvisoAdeudoService.RESULTADOS_ATENCION_VALIDOS.
    private String resultadoAtencion;
    // Detalle libre opcional.
    private String comentarioAtencion;
    // Folio del recibo (agua_recibo) que corresponde al pago, si ya se
    // capturó -- opcional incluso cuando resultadoAtencion = PAGADO (puede
    // no tenerlo a la mano en ese momento). Si se manda, el backend valida
    // que exista un recibo con ese folio antes de guardarlo.
    private Integer folioReciboVinculado;
    // Folio del convenio (agua_convenio) vinculado como referencia,
    // opcional -- independiente de folioReciboVinculado, se pueden mandar
    // ambos a la vez. El backend valida que exista un convenio con ese
    // folio antes de guardarlo.
    private Integer folioConvenioVinculado;
    // Obligatoria cuando resultadoAtencion = CONVENIO: fecha en la que el
    // usuario se comprometió a pagar. Mientras no venza, se pausa el
    // Segundo aviso para este usuario (ver AvisoAdeudoService.marcarAtendida()
    // y enriquecerConUltimoAviso()).
    private LocalDate fechaCompromiso;
}
