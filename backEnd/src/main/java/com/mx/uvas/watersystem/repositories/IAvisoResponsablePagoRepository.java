package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AvisoResponsablePagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IAvisoResponsablePagoRepository extends JpaRepository<AvisoResponsablePagoEntity, Integer> {

    // Para asignar el siguiente folio consecutivo -- secuencia propia,
    // independiente de la de las demás cartas.
    @Query("SELECT MAX(a.folioNotificacion) FROM AvisoResponsablePagoEntity a")
    Integer findMaxFolio();

    // Historial completo (activos + cancelados) -- el frontend oculta los
    // cancelados por default, mismo patrón que las demás cartas.
    List<AvisoResponsablePagoEntity> findByEstatusInOrderByFolioNotificacionDesc(List<Integer> estatuses);

    // Historial de una casa en particular -- para el acordeón "Responsables
    // de pago" en la ficha de la casa (house-details).
    List<AvisoResponsablePagoEntity> findByCasa_CasaIdAndEstatusInOrderByFolioNotificacionDesc(Integer casaId, List<Integer> estatuses);
}
