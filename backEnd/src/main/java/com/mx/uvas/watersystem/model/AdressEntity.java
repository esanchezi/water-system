package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "direccion")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AdressEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer direccionId;
    private Integer noCasa;
    private String calle;
    private String numero;
    private String referencia;
    private String entrecalle1;
    private String entrecalle2;
    private String coordenadas;
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
            mappedBy = "address"
    )
    private Set<WaterUserEntity> waterUser;

    @OneToOne
    @JoinColumn(name="seccion_id", nullable=true)
    private CatalogOptionsEntity catSeccion;


   /* public void addWaterUser(WaterUserEntity user){
        if((Objects.isNull(this.waterUser))) this.waterUser = new HashSet<>();
        this.waterUser.add(user);
    }

    public void removeWaterUser(Integer id){
        if((Objects.isNull(this.waterUser))) this.waterUser = new HashSet<>();
        this.waterUser.removeIf(user->user.getId().equals(id));
    }
    public void updateWaterUser(){
        if((Objects.isNull(this.waterUser))) this.waterUser = new HashSet<>();
        this.waterUser.forEach(user -> user.setAddress(this));
    }*/
}
