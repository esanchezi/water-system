package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterAgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface IWaterAgreementRepository extends JpaRepository<WaterAgreementEntity, Integer> {

    @Query("SELECT wa FROM WaterAgreementEntity wa WHERE wa.estatus = 1 ORDER BY wa.fecha DESC")
    List<WaterAgreementEntity> findAllActivos();

    // Para validar el folio de convenio que se liga (opcional) al marcar un
    // aviso de adeudo como atendido -- ver AvisoAdeudoService.marcarAtendida().
    // No todos los convenios llevan folio (noFolio es nullable), por eso no
    // hay una constraint UNIQUE de por medio; se toma el primero que
    // coincida, igual que ya se hace con folioReciboVinculado.
    WaterAgreementEntity findFirstByNoFolio(Integer noFolio);

    @Query("SELECT wa " +
            "FROM WaterAgreementEntity wa " +
            "JOIN wa.waterUser wu " +
            "WHERE wa.estatus = 1 AND wu.noUsuario = :noUser " +
            "ORDER BY wa.fecha DESC")
    List<WaterAgreementEntity> findByNoUser(Integer noUser);

    // Convenios activos cuya fecha comprometida de pago todavía no vence --
    // mientras el usuario esté dentro de ese plazo, se pausa el Segundo
    // aviso (ya vino a hacer un arreglo con el comité). Ver
    // AvisoAdeudoService.enriquecerConUltimoAviso().
    @Query("SELECT wa " +
            "FROM WaterAgreementEntity wa " +
            "JOIN wa.waterUser wu " +
            "WHERE wa.estatus = 1 AND wu.aguaUsuarioId IN :aguaUsuarioIds " +
            "AND wa.fechaCompromisoPago IS NOT NULL AND wa.fechaCompromisoPago >= :hoy " +
            "ORDER BY wa.fechaCompromisoPago ASC")
    List<WaterAgreementEntity> findVigentesConCompromisoPorUsuarios(List<Integer> aguaUsuarioIds, LocalDate hoy);

}
