package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.PreregistroUsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IPreregistroUsuarioRepository extends JpaRepository<PreregistroUsuarioEntity, Integer> {

    // Preregistros de una casa en particular (pantalla de detalle de casa).
    List<PreregistroUsuarioEntity> findByWaterHouse_CasaIdOrderByFechaRegistroDesc(Integer casaId);

    // Todos los pendientes, de todas las casas -- útil para un reporte
    // general de "gente por evaluar" más adelante.
    List<PreregistroUsuarioEntity> findByEstatusOrderByFechaRegistroAsc(Integer estatus);

    // Negocios marcados en preregistro que NUNCA van a tener su propio
    // usuario de agua (ej. tiendita atendida por el usuario del domicilio)
    // -- se excluyen los ya "Convertidos" porque esos ya cuentan como
    // negocio directo en WaterUserEntity, para no contarlos dos veces.
    @Query("SELECT p FROM PreregistroUsuarioEntity p " +
            "LEFT JOIN FETCH p.giroNegocio " +
            "WHERE p.esNegocio = true AND p.estatus <> " + PreregistroUsuarioEntity.ESTATUS_CONVERTIDO)
    List<PreregistroUsuarioEntity> findAllNegociosNoConvertidos();

    // Preregistros con deuda aproximada capturada (aportaciones y/o
    // multas/recargos > 0), de todas las casas -- para el módulo de
    // deudores, como referencia de gente que aún no es usuario formal pero
    // ya se sabe que debe algo. Se excluyen los ya Convertidos porque esos
    // ya aparecen como deudor real (WaterUserEntity) si aplica.
    @Query("SELECT p FROM PreregistroUsuarioEntity p " +
            "LEFT JOIN FETCH p.waterHouse wh " +
            "LEFT JOIN FETCH wh.catCalle " +
            "WHERE (p.deudaAportaciones > 0 OR p.deudaMultasRecargos > 0) " +
            "AND p.estatus <> " + PreregistroUsuarioEntity.ESTATUS_CONVERTIDO)
    List<PreregistroUsuarioEntity> findAllConDeudaNoConvertidos();

    // Listado global (todas las casas) para el módulo de preregistro --
    // trae de una vez casa/calle, giro, motivo y grupo para no disparar
    // lazy-loading fila por fila.
    @Query("SELECT p FROM PreregistroUsuarioEntity p " +
            "LEFT JOIN FETCH p.waterHouse wh " +
            "LEFT JOIN FETCH wh.catCalle " +
            "LEFT JOIN FETCH p.giroNegocio " +
            "LEFT JOIN FETCH p.motivoNoUsuario " +
            "LEFT JOIN FETCH p.waterGroup " +
            "ORDER BY p.fechaRegistro DESC")
    List<PreregistroUsuarioEntity> findAllConDetalle();

    // Preregistros ya vinculados a un grupo en particular (ficha de grupo).
    @Query("SELECT p FROM PreregistroUsuarioEntity p " +
            "LEFT JOIN FETCH p.waterHouse wh " +
            "LEFT JOIN FETCH wh.catCalle " +
            "WHERE p.waterGroup.grupoId = :grupoId " +
            "ORDER BY p.fechaRegistro DESC")
    List<PreregistroUsuarioEntity> findByWaterGroup_GrupoId(Integer grupoId);

}
