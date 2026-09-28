package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

// Foto de evidencia de una revisión de usuario -- mismo patrón que
// AvisoAdeudoFotoEntity: no se guarda el archivo en la base de datos, solo
// la referencia (nombre en disco + a qué revisión pertenece); el archivo
// físico vive en la carpeta externa app.uploads.dir/revisiones-usuario.
@Entity
@Table(name = "agua_usuario_revision_foto")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterUserRevisionFotoEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fotoId;

    private String nombreArchivo;
    private String nombreOriginal;
    private String contentType;

    @Builder.Default
    private Integer estatus = 1;
    private LocalDateTime dateAdd;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revision_id", nullable = false)
    private WaterUserRevisionEntity revision;
}
