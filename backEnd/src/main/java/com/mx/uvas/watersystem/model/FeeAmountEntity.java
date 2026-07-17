package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

// Nota: idealmente esta tabla debería tener un unique constraint (cuota_id, vigencia),
// pero hay datos históricos duplicados sin depurar todavía. Mientras tanto la validación
// de "un monto por año" vive solo en FeeService.addAmount/updateAmount (a nivel app).
// Cuando se limpien los duplicados, agregar aquí:
// @Table(name = "cuota_monto", uniqueConstraints = @UniqueConstraint(columnNames = {"cuota_id", "vigencia"}))
@Entity
@Table(name = "cuota_monto")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FeeAmountEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cuotaMontoId;
    private Integer cuota;
    private Integer vigencia;
    private String observaciones;
    private Integer estatus;
    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuota_id")
    private FeeEntity fee;
}
