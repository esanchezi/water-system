package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AvisoPadronEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IAvisoPadronRepository extends JpaRepository<AvisoPadronEntity, Integer> {

    // Para asignar el siguiente folio consecutivo -- secuencia propia,
    // independiente de la de avisos de adeudo/bomba.
    @Query("SELECT MAX(a.folioNotificacion) FROM AvisoPadronEntity a")
    Integer findMaxFolio();

    // Historial completo (activos + cancelados) -- el frontend oculta los
    // cancelados por default, mismo patrón que avisos de adeudo/bomba.
    List<AvisoPadronEntity> findByEstatusInOrderByFolioNotificacionDesc(List<Integer> estatuses);
}
