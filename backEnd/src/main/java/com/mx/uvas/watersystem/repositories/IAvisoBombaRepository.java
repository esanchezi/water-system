package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AvisoBombaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IAvisoBombaRepository extends JpaRepository<AvisoBombaEntity, Integer> {

    // Para asignar el siguiente folio consecutivo -- secuencia propia,
    // independiente de la de avisos de adeudo.
    @Query("SELECT MAX(a.folioNotificacion) FROM AvisoBombaEntity a")
    Integer findMaxFolio();

    // Historial completo (activos + cancelados) -- el frontend oculta los
    // cancelados por default, mismo patrón que avisos de adeudo.
    List<AvisoBombaEntity> findByEstatusInOrderByFolioNotificacionDesc(List<Integer> estatuses);
}
