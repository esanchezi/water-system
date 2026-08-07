package com.mx.uvas.watersystem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CatalogOptionsDto implements Serializable {

    private Integer catalogoOpcionesId;
    private Integer catalogoId;
    private String nombre;
    private String descripcion;
    private Integer estatus;

    // Zona a la que pertenece esta opción (uso pensado para el catálogo
    // Calle: varias calles -> una zona, ej. "La Barca" agrupa Azucena,
    // Jazmín, Orquídea...). Opcional y genérico -- no amarrado a un
    // catálogo en particular. Null si no aplica o no se ha asignado.
    private Integer zonaId;
    private String zonaNombre;
}
