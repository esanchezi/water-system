package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// Foto de uno o varios recibos físicos (papel) del comité, subida para
// revisarlos contra lo que ya está capturado en el sistema. El archivo en
// sí vive fuera del proyecto (ver app.uploads.dir, mismo patrón que
// ValvulaFotoEntity), aquí solo la referencia. Una sola foto puede tener
// varias revisiones (una por cada recibo que aparezca en la imagen).
@Entity
@Table(name = "agua_recibo_revision_foto")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ReciboRevisionFotoEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251701L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fotoId;

    // Nombre único (UUID + extensión) con el que se guardó en disco.
    private String nombreArchivo;

    // Nombre original del archivo tal como se subió, solo de referencia.
    private String nombreOriginal;

    private String contentType;

    // Nota libre sobre la foto, ej. "carpeta 2023, folios 100-103".
    private String observaciones;

    @Builder.Default
    private Integer estatus = 1;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;

    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "foto"
    )
    private Set<ReciboRevisionEntity> revisiones;

    public void addRevision(ReciboRevisionEntity revision) {
        if (this.revisiones == null) this.revisiones = new HashSet<>();
        this.revisiones.add(revision);
    }
}
