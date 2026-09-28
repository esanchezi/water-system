package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.RenunciaTemporalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IRenunciaTemporalRepository extends JpaRepository<RenunciaTemporalEntity, Integer> {

    @Query("SELECT MAX(r.folio) FROM RenunciaTemporalEntity r")
    Integer findMaxFolio();

    List<RenunciaTemporalEntity> findByEstatusInOrderByDateAddDesc(List<Integer> estatuses);

    List<RenunciaTemporalEntity> findByWaterUser_AguaUsuarioIdAndEstatusOrderByDateAddDesc(Integer aguaUsuarioId, Integer estatus);

    // La renuncia ACTIVA (no cancelada) y SIN reconectar todavía de un
    // usuario -- si existe, el usuario está actualmente en renuncia
    // temporal. Solo debería haber una a la vez por usuario, pero se
    // devuelve lista por si acaso (se usa la primera).
    List<RenunciaTemporalEntity> findByWaterUser_AguaUsuarioIdAndEstatusAndFechaReconexionIsNull(Integer aguaUsuarioId, Integer estatus);

    // Para excluir en lote a los usuarios que están en renuncia temporal
    // de la lista de candidatos a carta de adeudo (ver AdeudoLuzService).
    @Query("SELECT r FROM RenunciaTemporalEntity r WHERE r.estatus = 1 AND r.fechaReconexion IS NULL "
            + "AND r.waterUser.aguaUsuarioId IN :aguaUsuarioIds")
    List<RenunciaTemporalEntity> findActivasPorUsuarios(@Param("aguaUsuarioIds") List<Integer> aguaUsuarioIds);
}
