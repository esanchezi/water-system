package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "agua_usuario_aviso")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WaterUserNoticeEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aguaUsuarioAvisoId;
    private String aviso;
    private String comentario;
    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agua_usuario_id")
    private WaterUserEntity waterUser;

    @OneToOne
    @JoinColumn(name="aviso_estatus_id", nullable=true)
    private CatalogOptionsEntity estatusAviso;

}
