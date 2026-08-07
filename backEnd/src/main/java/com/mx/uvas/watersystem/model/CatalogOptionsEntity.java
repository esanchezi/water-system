package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "catalogo_opciones")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CatalogOptionsEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -4310027227752446841L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer catalogoOpcionesId;
    private String nombre;
    private String descripcion;
    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalogo_id")
    private CatalogEntity catalog;

    // Zona a la que pertenece esta opción -- pensado para agrupar varias
    // calles bajo una misma zona (ej. "La Barca" = Azucena, Jazmín,
    // Orquídea...). Es una referencia a otra fila de esta misma tabla
    // (en la práctica, una opción del catálogo SECCIONES_COLONIA que ya
    // existía -- se reutiliza en vez de crear un catálogo de zonas nuevo),
    // por eso es auto-referenciada y opcional: no todas las opciones de
    // todos los catálogos necesitan una zona.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = true)
    private CatalogOptionsEntity zona;
}
