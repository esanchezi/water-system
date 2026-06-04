package com.mx.uvas.watersystem.model;

import com.mx.uvas.watersystem.utils.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_roles")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserRoleEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userRolId;

    private String description;

}
