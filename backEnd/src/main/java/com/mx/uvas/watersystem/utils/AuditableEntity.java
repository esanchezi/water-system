package com.mx.uvas.watersystem.utils;

import com.mx.uvas.watersystem.model.UserEntity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class AuditableEntity {

    private Integer estatus;
    private LocalDateTime dateAdd;
    private LocalDateTime dateUpdate;

    @ManyToOne
    @JoinColumn(name = "user_id_add" ) //, referencedColumnName = "user_id_add"
    private UserEntity userIdAdd;

    @ManyToOne
    @JoinColumn(name = "user_id_update" ) //, referencedColumnName = "user_id_update"
    private UserEntity userIdUpdate;

}
