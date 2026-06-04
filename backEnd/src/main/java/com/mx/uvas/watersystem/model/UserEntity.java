package com.mx.uvas.watersystem.model;

import com.mx.uvas.watersystem.utils.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserEntity  extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @ManyToOne
    @JoinColumn(name = "persona_id")
    private PersonEntity person;

    @ManyToOne
    @JoinColumn(name = "user_rol_id")
    private UserRoleEntity userRole;

    private String email;
    private byte[] password;
}
