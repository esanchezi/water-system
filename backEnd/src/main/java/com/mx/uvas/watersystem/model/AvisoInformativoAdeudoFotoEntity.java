package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

// Foto de respaldo tomada al momento de entregar (o intentar entregar) un
// Aviso Informativo de Adeudo -- mismo patrón que AvisoBombaFotoEntity /
// AvisoAdeudoFotoEntity: el archivo vive en disco (app.uploads.dir), no en
// la base de datos.
@Entity
@Table(name = "agua_aviso_informativo_adeudo_foto")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AvisoInformativoAdeudoFotoEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950252102L;

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
    @JoinColumn(name = "aviso_informativo_adeudo_id", nullable = false)
    private AvisoInformativoAdeudoEntity avisoInformativoAdeudo;
}
