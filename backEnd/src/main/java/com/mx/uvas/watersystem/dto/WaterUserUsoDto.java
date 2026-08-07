package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

// DTO chico a propósito -- solo los campos de la clasificación de "uso"
// (estatus de la toma, habita/renta, negocio/giro/local). Se usa desde un
// endpoint dedicado (updateUso) para poder actualizar SOLO estos campos
// sin arriesgar pisar noUsuario/alias/cuota/etc. de un usuario, ya que
// este componente se reutiliza en contextos (ej. ficha de casa) donde no
// se tiene cargado el resto de los datos del usuario.
@Data
public class WaterUserUsoDto implements Serializable {
    private Integer aguaUsuarioId;
    private Integer estatusTomaId;
    private Boolean tieneToma;
    private Boolean habitaDomicilio;
    private Boolean inmuebleRenta;
    private Boolean esNegocio;
    private Integer giroNegocioId;
    private Boolean tieneLocal;
    private Boolean localRentadoPorUsuario;
}
