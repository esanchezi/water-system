package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterUserChargeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IWaterUserChargeRepository extends JpaRepository<WaterUserChargeEntity, Integer> {

    @Query("SELECT wuc " +
            "FROM WaterUserChargeEntity wuc " +
            "JOIN wuc.waterUser wu " +
            "WHERE wuc.estatus = 1 AND wu.noUsuario = :noUser " +
            "ORDER BY wuc.fecha DESC")
    List<WaterUserChargeEntity> findByNoUser(Integer noUser);

    // Todos los cargos activos de todos los usuarios, para reportes que
    // calculan el saldo pendiente (getSaldo()) sin hacer una query por usuario.
    List<WaterUserChargeEntity> findByEstatus(Integer estatus);

    // Todos los cargos activos de todos los usuarios que pertenecen a un
    // grupo -- para la lista de multas/cargos de todo el grupo (en vez de
    // tener que entrar a cada usuario por separado).
    @Query("SELECT wuc " +
            "FROM WaterUserChargeEntity wuc " +
            "JOIN wuc.waterUser wu " +
            "WHERE wuc.estatus = 1 AND wu.waterGroup.grupoId = :grupoId " +
            "ORDER BY wuc.fecha DESC")
    List<WaterUserChargeEntity> findByGrupoId(Integer grupoId);

}
