package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

// Foto de respaldo tomada al momento de entregar (o intentar entregar) un
// aviso de actualización de padrón -- ej. cuando no se encontró a nadie o
// se negaron a firmar. Mismo patrón que AvisoAdeudoFotoEntity: el archivo
// en sí vive en disco, fuera de la carpeta del proyecto (ver
// app.uploads.dir), no en la base de datos. Aquí solo se guarda con qué
// nombre quedó guardado y a qué aviso pertenece.
@Entity
@Table(name = "agua_aviso_padron_foto")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AvisoPadronFotoEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251901L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fotoId;

    // Nombre único generado (UUID + extensión) con el que se guardó en
    // disco -- no el nombre original, para no chocar entre archivos de
    // distinta gente con el mismo nombre.
    private String nombreArchivo;

    // Nombre original del archivo tal como lo subieron, solo de referencia.
    private String nombreOriginal;

    private String contentType;

    @Builder.Default
    private Integer estatus = 1;
    private LocalDateTime dateAdd;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aviso_padron_id", nullable = false)
    private AvisoPadronEntity avisoPadron;
}
