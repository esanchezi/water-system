package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AvisoAdeudoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IAvisoAdeudoRepository extends JpaRepository<AvisoAdeudoEntity, Integer> {

    // Para asignar el siguiente folio consecutivo -- ver AvisoAdeudoService.
    // NULL si todavía no hay ningún aviso generado (primer folio = 1).
    @Query("SELECT MAX(a.folioNotificacion) FROM AvisoAdeudoEntity a")
    Integer findMaxFolio();

    List<AvisoAdeudoEntity> findByEstatusOrderByFolioNotificacionDesc(Integer estatus);

    List<AvisoAdeudoEntity> findByWaterUser_AguaUsuarioIdAndEstatusOrderByFolioNotificacionDesc(Integer aguaUsuarioId, Integer estatus);

    // Para el control de Primer/Segundo aviso en la pantalla de candidatos:
    // trae TODOS los avisos activos de un grupo de usuarios, ordenados por
    // folio (consecutivo global) descendente -- como folioNotificacion es
    // el mismo consecutivo para todos, el primer registro de cada usuario
    // en esta lista ya es su aviso más reciente, sin necesidad de agrupar.
    List<AvisoAdeudoEntity> findByWaterUser_AguaUsuarioIdInAndEstatusOrderByFolioNotificacionDesc(
            List<Integer> aguaUsuarioIds, Integer estatus);

    // Historial completo (activas + canceladas) -- el frontend oculta las
    // canceladas por default y las muestra solo si se filtra explícito por
    // ellas (mismo patrón que los usuarios dados de baja en candidatos).
    List<AvisoAdeudoEntity> findByEstatusInOrderByFolioNotificacionDesc(List<Integer> estatuses);

    // Cartas ya entregadas a este usuario que todavía no se marcan como
    // atendidas (no se ha hecho el cobro/trámite correspondiente) -- para
    // la alerta que se muestra al consultar la ficha del usuario, y para el
    // checklist de "Agregar recibo". Excluye canceladas, y también excluye
    // un Primer aviso si ya se le generó un Segundo aviso activo después
    // (folio mayor) -- ya no tiene caso cobrarlo/marcarlo atendido por
    // separado, el seguimiento sigue con el Segundo.
    @Query("SELECT a FROM AvisoAdeudoEntity a WHERE a.waterUser.aguaUsuarioId = :aguaUsuarioId "
            + "AND a.estatus = 1 AND a.fechaEntrega IS NOT NULL AND a.fechaAtencion IS NULL "
            + "AND NOT (a.tipoAviso = 'PRIMERO' AND EXISTS ("
            + "  SELECT 1 FROM AvisoAdeudoEntity b WHERE b.waterUser.aguaUsuarioId = a.waterUser.aguaUsuarioId "
            + "  AND b.tipoAviso = 'SEGUNDO' AND b.estatus = 1 AND b.folioNotificacion > a.folioNotificacion"
            + ")) "
            + "ORDER BY a.fechaEntrega DESC")
    List<AvisoAdeudoEntity> findPendientesDeAtencionPorUsuario(@Param("aguaUsuarioId") Integer aguaUsuarioId);
}
