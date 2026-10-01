package com.mx.uvas.watersystem.repositories;

import com.mx.uvas.watersystem.model.WaterUserChargeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IWaterUserChargeRepository extends JpaRepository<WaterUserChargeEntity, Integer> {

    @Query("SELECT wuc " +
            "FROM WaterUserChargeEntity wuc " +
            "JOIN wuc.waterUser wu " +
            "WHERE wuc.estatus = 1 AND wu.noUsuario = :noUser " +
            "ORDER BY wuc.fecha DESC")
    List<WaterUserChargeEntity> findByNoUser(Integer noUser);

    // Todos los cargos activos de todos los usuarios, para reportes que
    // calculan el saldo pendiente (getSaldo()) sin hacer una query por usuario.
    List<WaterUserChargeEntity> findByEstatus(Integer estatus);

    // Igual, pero acotado a un grupo de usuarios -- para no traer los
    // cargos de TODO el sistema cuando solo interesan unos cuantos (ver
    // AdeudoLuzService, "multa acumulada" de la carta de adeudo).
    List<WaterUserChargeEntity> findByEstatusAndWaterUser_AguaUsuarioIdIn(Integer estatus, List<Integer> aguaUsuarioIds);

    // Todos los cargos activos de todos los usuarios que pertenecen a un
    // grupo -- para la lista de multas/cargos de todo el grupo (en vez de
    // tener que entrar a cada usuario por separado).
    @Query("SELECT wuc " +
            "FROM WaterUserChargeEntity wuc " +
            "JOIN wuc.waterUser wu " +
            "WHERE wuc.estatus = 1 AND wu.waterGroup.grupoId = :grupoId " +
            "ORDER BY wuc.fecha DESC")
    List<WaterUserChargeEntity> findByGrupoId(Integer grupoId);

    // Para no duplicar el cargo de Mantenimiento de cajón (Art. 10) si se
    // genera más de una carta para el mismo usuario -- a diferencia del
    // cargo de Aviso (uno nuevo por cada carta), este es una sola vez por
    // año adeudado, así que antes de crearlo se busca por descripción
    // exacta ("Mantenimiento de cajón 2024", etc.) para ese usuario. Ver
    // AvisoAdeudoService.crearCargoMantenimiento().
    boolean existsByWaterUser_AguaUsuarioIdAndConcepto_NombreAndDescripcionAndEstatus(
            Integer aguaUsuarioId, String nombreConcepto, String descripcion, Integer estatus);

    // v11 (sept. 2026, pedido explícito de Ely): para saber si el cargo
    // "Aviso" de una carta en particular (descripción exacta, ver
    // crearCargoAviso()) ya se saldó o sigue pendiente -- ver
    // AvisoAdeudoService.pendientesDeAtencion(). Mismo criterio de búsqueda
    // que el exists de arriba, pero trayendo la entidad completa para poder
    // leer getSaldo().
    Optional<WaterUserChargeEntity> findFirstByWaterUser_AguaUsuarioIdAndConcepto_NombreAndDescripcionAndEstatus(
            Integer aguaUsuarioId, String nombreConcepto, String descripcion, Integer estatus);
}
