package com.mx.uvas.watersystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "asamblea")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AssemblyEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer asambleaId;
    private LocalDate date;
    private String observaciones;
    private String purpose;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true,
            mappedBy = "assemblyEntity"
    )
    private Set<AssemblyAttendanceEntity> assemblyAttendanceEntity;
}
