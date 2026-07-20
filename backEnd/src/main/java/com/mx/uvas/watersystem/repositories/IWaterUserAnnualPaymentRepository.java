package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterUserAnnualPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IWaterUserAnnualPaymentRepository extends JpaRepository <WaterUserAnnualPaymentEntity,Integer>{

    boolean existsByWaterUser_AguaUsuarioIdAndAnio(Integer aguaUsuarioId, Integer anio);

    boolean existsByWaterUser_AguaUsuarioIdAndAnioAndEstatus(Integer aguaUsuarioId, Integer anio, Integer estatus);

    // Usuarios que ya tienen marcado como pagado un año en particular —
    // usado por el reporte de deudores para saber a quién excluir.
    List<WaterUserAnnualPaymentEntity> findByAnioAndEstatus(Integer anio, Integer estatus);

    // Historial completo (activos y dados de baja) de un usuario, para la
    // ficha de usuario donde se administra manualmente.
    @Query("SELECT p FROM WaterUserAnnualPaymentEntity p " +
            "JOIN p.waterUser wu " +
            "WHERE wu.noUsuario = :noUser " +
            "ORDER BY p.anio DESC")
    List<WaterUserAnnualPaymentEntity> findByNoUser(@Param("noUser") Integer noUser);

}