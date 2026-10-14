package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AvisoMedidorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IAvisoMedidorRepository extends JpaRepository<AvisoMedidorEntity, Integer> {

    // Para asignar el siguiente folio consecutivo -- secuencia propia,
    // independiente de la de los demás avisos.
    @Query("SELECT MAX(a.folioNotificacion) FROM AvisoMedidorEntity a")
    Integer findMaxFolio();

    // Historial completo (activos + cancelados) -- el frontend oculta los
    // cancelados por default, mismo patrón que el resto de avisos.
    List<AvisoMedidorEntity> findByEstatusInOrderByFolioNotificacionDesc(List<Integer> estatuses);

    // Historial completo de un usuario específico -- para el acordeón
    // "Cartas generadas" en su ficha.
    List<AvisoMedidorEntity> findByWaterUser_AguaUsuarioIdAndEstatusInOrderByFolioNotificacionDesc(Integer aguaUsuarioId, List<Integer> estatuses);
}
