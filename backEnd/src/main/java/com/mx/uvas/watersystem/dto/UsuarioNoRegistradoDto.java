package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// Persona SIN usuario en el sistema (censo aún en proceso) a la que de
// todos modos se le quiere entregar una carta de adeudo -- solo se captura
// su nombre y, si se tiene, su dirección. El adeudo no se calcula contra
// recibos reales (no hay registro de pagos), sino con la cuota fija anual
// definida en AvisoAdeudoService.CUOTAS_FIJAS_NO_REGISTRADO.
@Data
public class UsuarioNoRegistradoDto implements Serializable {
    private String nombre;
    private String direccion;
}
