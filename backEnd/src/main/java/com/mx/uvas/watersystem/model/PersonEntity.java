package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "persona")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PersonEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer personaId;
    private String nombre;
    private String nombre2;
    private String app;
    private String apm;
    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            mappedBy = "person"
    )
    private Set<WaterUserEntity> waterUser;

    public void addWaterUser(WaterUserEntity user){
        if((Objects.isNull(this.waterUser))) this.waterUser = new HashSet<>();
        this.waterUser.add(user);
    }

    public void removeWaterUser(Integer id){
        if((Objects.isNull(this.waterUser))) this.waterUser = new HashSet<>();
        this.waterUser.removeIf(user->user.getAguaUsuarioId().equals(id));
    }
    public void updateWaterUser(){
        if((Objects.isNull(this.waterUser))) this.waterUser = new HashSet<>();
        this.waterUser.forEach(user -> user.setPerson(this));
    }
}
