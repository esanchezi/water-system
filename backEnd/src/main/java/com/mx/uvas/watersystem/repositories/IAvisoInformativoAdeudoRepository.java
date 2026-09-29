package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.AvisoInformativoAdeudoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IAvisoInformativoAdeudoRepository extends JpaRepository<AvisoInformativoAdeudoEntity, Integer> {

    // Secuencia de folio propia, totalmente independiente de la de Cartas
    // de adeudo (agua_aviso_adeudo) -- ver clarificación de Ely: "cada
    // proceso tiene que ser independiente en folios".
    @Query("SELECT MAX(a.folioNotificacion) FROM AvisoInformativoAdeudoEntity a")
    Integer findMaxFolio();

    List<AvisoInformativoAdeudoEntity> findByEstatusInOrderByFolioNotificacionDesc(List<Integer> estatuses);

    // Historial completo de un usuario específico -- para el acordeón
    // "Cartas generadas" en su ficha.
    List<AvisoInformativoAdeudoEntity> findByWaterUser_AguaUsuarioIdAndEstatusInOrderByFolioNotificacionDesc(Integer aguaUsuarioId, List<Integer> estatuses);
}
